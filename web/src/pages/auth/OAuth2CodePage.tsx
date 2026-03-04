import {AuthCardLayout} from "./AuthorizationPage.tsx";
import {useNavigate} from "react-router-dom";
import {getQueryString} from "../../utils/url.utils.ts";
import {menuPathDashboard, menuPathLogin} from "../../router";
import {Button, Card, Avatar, Tabs, Form, Input, message} from "antd";
import {useState} from "react";
import {loginByOAuth2Code, bindOAuthAccount, registerFromOAuthAccount} from "../../api/auth.api.ts";
import {setUserAuthentication} from "../../utils/token.utils.ts";
import type {LoginResponse, OAuth2LoginResponse, OAuth2UserInfo} from "../../types/auth.types.ts";
import {GithubOutlined, UserOutlined, LockOutlined} from "@ant-design/icons";

function isLoginResponse(res: OAuth2LoginResponse): res is LoginResponse {
    return (res as LoginResponse).token !== undefined;
}

interface RegisterFormData {
    username: string;
    password: string;
    confirmPassword: string;
    nickname: string;
}

interface BindFormData {
    username: string;
    password: string;
}

interface RegisterTabProps {
    userInfo: OAuth2UserInfo;
}

interface BindTabProps {
    userInfo: OAuth2UserInfo;
}

function RegisterTab({userInfo}: RegisterTabProps) {
    const navigate = useNavigate();
    const [form] = Form.useForm();
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleRegister = (values: RegisterFormData) => {
        setIsSubmitting(true);
        registerFromOAuthAccount({
            oauthAccountId: userInfo.oauthAccountId,
            username: values.username,
            password: values.password,
            nickname: values.nickname
        })
            .then((res) => {
                if (res.data) {
                    void message.success('注册并绑定成功');
                    setUserAuthentication(res.data.token, res.data.expiresIn);
                    navigate(menuPathDashboard);
                } else {
                    void message.error(res.message || '注册失败');
                }
            })
            .catch(() => {
                void message.error('注册失败，请重试');
            })
            .finally(() => {
                setIsSubmitting(false);
            });
    };

    return (
        <Form
            form={form}
            layout="vertical"
            onFinish={handleRegister}
            autoComplete="off"
            initialValues={{
                nickname: userInfo.nickname
            }}
        >
            <Form.Item
                name="username"
                rules={[{ required: true, message: '请输入用户名' }]}
            >
                <Input
                    prefix={<UserOutlined className="text-gray-400 mr-2" />}
                    placeholder="用户名"
                    size="large"
                    className="rounded-xl"
                />
            </Form.Item>
            <Form.Item
                name="password"
                rules={[{ required: true, message: '请输入密码' }]}
            >
                <Input.Password
                    prefix={<LockOutlined className="text-gray-400 mr-2" />}
                    placeholder="密码"
                    size="large"
                    className="rounded-xl"
                />
            </Form.Item>
            <Form.Item
                name="confirmPassword"
                dependencies={['password']}
                rules={[
                    { required: true, message: '请确认密码' },
                    ({ getFieldValue }) => ({
                        validator(_, value) {
                            if (!value || getFieldValue('password') === value) {
                                return Promise.resolve();
                            }
                            return Promise.reject(new Error('两次输入的密码不一致'));
                        },
                    }),
                ]}
            >
                <Input.Password
                    prefix={<LockOutlined className="text-gray-400 mr-2" />}
                    placeholder="确认密码"
                    size="large"
                    className="rounded-xl"
                />
            </Form.Item>
            <Form.Item
                name="nickname"
                rules={[{ required: true, message: '请输入昵称' }]}
            >
                <Input
                    prefix={<UserOutlined className="text-gray-400 mr-2" />}
                    placeholder="昵称"
                    size="large"
                    className="rounded-xl"
                />
            </Form.Item>
            <Form.Item className="mb-0">
                <Button
                    type="primary"
                    htmlType="submit"
                    size="large"
                    loading={isSubmitting}
                    className="w-full h-12 text-base font-semibold shadow-lg rounded-xl border-none"
                >
                    注册并绑定
                </Button>
            </Form.Item>
        </Form>
    );
}

function BindTab({userInfo}: BindTabProps) {
    const navigate = useNavigate();
    const [form] = Form.useForm();
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleBind = (values: BindFormData) => {
        setIsSubmitting(true);
        bindOAuthAccount({
            oauthAccountId: userInfo.oauthAccountId,
            username: values.username,
            password: values.password
        })
            .then((res) => {
                if (res.data) {
                    void message.success('绑定成功');
                    setUserAuthentication(res.data.token, res.data.expiresIn);
                    navigate(menuPathDashboard);
                } else {
                    void message.error(res.message || '绑定失败');
                }
            })
            .catch(() => {
                void message.error('绑定失败，请重试');
            })
            .finally(() => {
                setIsSubmitting(false);
            });
    };

    return (
        <Form
            form={form}
            layout="vertical"
            onFinish={handleBind}
            autoComplete="off"
        >
            <Form.Item
                name="username"
                rules={[{ required: true, message: '请输入用户名' }]}
            >
                <Input
                    prefix={<UserOutlined className="text-gray-400 mr-2" />}
                    placeholder="用户名"
                    size="large"
                    className="rounded-xl"
                />
            </Form.Item>
            <Form.Item
                name="password"
                rules={[{ required: true, message: '请输入密码' }]}
            >
                <Input.Password
                    prefix={<LockOutlined className="text-gray-400 mr-2" />}
                    placeholder="密码"
                    size="large"
                    className="rounded-xl"
                />
            </Form.Item>
            <Form.Item className="mb-0">
                <Button
                    type="primary"
                    htmlType="submit"
                    size="large"
                    loading={isSubmitting}
                    className="w-full h-12 text-base font-semibold shadow-lg rounded-xl border-none"
                >
                    绑定账号
                </Button>
            </Form.Item>
        </Form>
    );
}

export function OAuth2CodePage() {
    const navigate = useNavigate();

    const code = getQueryString('code');
    const state = getQueryString('state');

    const [isProcessing, setProcessing] = useState(false);
    const [isProcessed, setProcessed] = useState(false);
    const [userInfo, setUserInfo] = useState<OAuth2UserInfo | null>(null);
    const [activeTab, setActiveTab] = useState<'register' | 'bind'>('register');

    if (!code || !state) {
        void message.error("无效的登录信息");
        navigate(menuPathLogin);
        return null;
    }

    const handleLogin = () => {
        if (isProcessing || isProcessed) return;

        setProcessing(true);

        loginByOAuth2Code(code, state)
            .then((res) => {
                if (res.data) {
                    if (isLoginResponse(res.data)) {
                        void message.success('登录成功');
                        setUserAuthentication(res.data.token, res.data.expiresIn);
                        navigate(menuPathDashboard);
                    } else {
                        setUserInfo(res.data as OAuth2UserInfo);
                    }
                } else {
                    void message.error('登录失败，未知错误');
                }
            })
            .catch(() => {
                void message.error('登录失败');
            })
            .finally(() => {
                setProcessing(false);
                setProcessed(true);
            });
    }

    if (userInfo) {
        const tabItems = [
            {
                key: 'register',
                label: '注册新账号',
                children: <RegisterTab userInfo={userInfo} />
            },
            {
                key: 'bind',
                label: '绑定已有账号',
                children: <BindTab userInfo={userInfo} />
            }
        ];

        return (
            <AuthCardLayout
                title="绑定账号"
                subtitle="该第三方账号尚未绑定，请选择操作"
            >
                <Card className="mb-6 bg-gray-50 border-gray-200">
                    <div className="flex items-center">
                        <Avatar
                            size={64}
                            src={userInfo.avatar}
                            icon={<GithubOutlined />}
                            className="mr-4"
                        />
                        <div>
                            <div className="text-lg font-semibold">{userInfo.nickname}</div>
                            <div className="text-gray-500 text-sm flex items-center gap-2">
                                <GithubOutlined />
                                {userInfo.platform}
                            </div>
                        </div>
                    </div>
                </Card>
                <Tabs
                    activeKey={activeTab}
                    onChange={(key) => setActiveTab(key as 'register' | 'bind')}
                    items={tabItems}
                    centered
                    className="w-full"
                />
            </AuthCardLayout>
        );
    }

    return (
        <AuthCardLayout
            title="第三方登录验证"
            subtitle="请点击下方按钮完成登录验证"
        >
            <Button
                type="primary"
                size="large"
                loading={isProcessing}
                disabled={isProcessed && !userInfo}
                className="w-full h-12 text-base font-semibold shadow-lg rounded-xl border-none active:scale-[0.98] transition-all"
                onClick={handleLogin}
            >
                {isProcessing ? '验证中...' : '确认登录'}
            </Button>
        </AuthCardLayout>
    )
}
