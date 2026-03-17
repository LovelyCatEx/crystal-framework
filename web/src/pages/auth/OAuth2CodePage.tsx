import {AuthCardLayout} from "./AuthorizationPage.tsx";
import {useNavigate} from "react-router-dom";
import {getQueryString} from "@/utils/url.utils.ts";
import {menuPathDashboard, menuPathLogin} from "@/router";
import {Avatar, Button, Card, Form, Input, message, Space, Tabs} from "antd";
import {useEffect, useState} from "react";
import {useTranslation} from "react-i18next";
import {bindOAuthAccount, loginByOAuth2Code, registerFromOAuthAccount} from "@/api/auth.api.ts";
import {setUserAuthentication} from "@/utils/token.utils.ts";
import type {LoginResponse, OAuth2LoginResponse, OAuth2UserInfo} from "@/types/auth.types.ts";
import {GithubOutlined, LockOutlined, UserOutlined} from "@ant-design/icons";
import PlatformIcon from "../../components/PlatformIcon.tsx";
import {getOAuthPlatformByName} from "@/types/oauth-account.types.ts";
import {useLoggedUser} from "@/compositions/use-logged-user.ts";

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

interface BindCurrentUserTabProps {
    userInfo: OAuth2UserInfo;
    currentUser: { nickname: string; username: string; avatar?: string | null };
}

function RegisterTab({userInfo}: RegisterTabProps) {
    const { t } = useTranslation();
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
                    void message.success(t('pages.auth.oauth2.bind.messages.registerBindSuccess'));
                    setUserAuthentication(res.data.token, res.data.expiresIn);
                    navigate(menuPathDashboard);
                } else {
                    void message.error(res.message || t('pages.auth.oauth2.bind.messages.registerFailed'));
                }
            })
            .catch(() => {
                void message.error(t('pages.auth.oauth2.bind.messages.registerFailed'));
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
                rules={[
                    {required: true, message: t('pages.auth.oauth2.bind.register.username.required')},
                    {pattern: /^[a-zA-Z0-9_-]+$/, message: t('pages.auth.oauth2.bind.register.username.pattern')},
                ]}
            >
                <Input
                    prefix={<UserOutlined className="text-gray-400 mr-2" />}
                    placeholder={t('pages.auth.oauth2.bind.register.username.placeholder')}
                    size="large"
                    className="rounded-xl"
                />
            </Form.Item>
            <Form.Item
                name="password"
                rules={[{ required: true, message: t('pages.auth.oauth2.bind.register.password.required') }]}
            >
                <Input.Password
                    prefix={<LockOutlined className="text-gray-400 mr-2" />}
                    placeholder={t('pages.auth.oauth2.bind.register.password.placeholder')}
                    size="large"
                    className="rounded-xl"
                />
            </Form.Item>
            <Form.Item
                name="confirmPassword"
                dependencies={['password']}
                rules={[
                    { required: true, message: t('pages.auth.oauth2.bind.register.confirmPassword.required') },
                    ({ getFieldValue }) => ({
                        validator(_, value) {
                            if (!value || getFieldValue('password') === value) {
                                return Promise.resolve();
                            }
                            return Promise.reject(new Error(t('pages.auth.oauth2.bind.register.confirmPassword.mismatch')));
                        },
                    }),
                ]}
            >
                <Input.Password
                    prefix={<LockOutlined className="text-gray-400 mr-2" />}
                    placeholder={t('pages.auth.oauth2.bind.register.confirmPassword.placeholder')}
                    size="large"
                    className="rounded-xl"
                />
            </Form.Item>
            <Form.Item
                name="nickname"
                rules={[{ required: true, message: t('pages.auth.oauth2.bind.register.nickname.required') }]}
            >
                <Input
                    prefix={<UserOutlined className="text-gray-400 mr-2" />}
                    placeholder={t('pages.auth.oauth2.bind.register.nickname.placeholder')}
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
                    {t('pages.auth.oauth2.bind.register.submit')}
                </Button>
            </Form.Item>
        </Form>
    );
}

function BindTab({userInfo}: BindTabProps) {
    const { t } = useTranslation();
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
                    void message.success(t('pages.auth.oauth2.bind.messages.bindSuccess'));
                    setUserAuthentication(res.data.token, res.data.expiresIn);
                    navigate(menuPathDashboard);
                } else {
                    void message.error(res.message || t('pages.auth.oauth2.bind.messages.bindFailed'));
                }
            })
            .catch(() => {
                void message.error(t('pages.auth.oauth2.bind.messages.bindRetry'));
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
                rules={[
                    {required: true, message: t('pages.auth.oauth2.bind.bindExisting.username.required')},
                    {pattern: /^[a-zA-Z0-9_@.-]+$/, message: t('pages.auth.oauth2.bind.bindExisting.username.pattern')},
                ]}
            >
                <Input
                    prefix={<UserOutlined className="text-gray-400 mr-2" />}
                    placeholder={t('pages.auth.oauth2.bind.bindExisting.username.placeholder')}
                    size="large"
                    className="rounded-xl"
                />
            </Form.Item>
            <Form.Item
                name="password"
                rules={[{ required: true, message: t('pages.auth.oauth2.bind.bindExisting.password.required') }]}
            >
                <Input.Password
                    prefix={<LockOutlined className="text-gray-400 mr-2" />}
                    placeholder={t('pages.auth.oauth2.bind.bindExisting.password.placeholder')}
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
                    {t('pages.auth.oauth2.bind.bindExisting.submit')}
                </Button>
            </Form.Item>
        </Form>
    );
}

function BindCurrentUserTab({userInfo, currentUser}: BindCurrentUserTabProps) {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleBind = () => {
        setIsSubmitting(true);
        bindOAuthAccount({
            oauthAccountId: userInfo.oauthAccountId
        })
            .then((res) => {
                if (res.data) {
                    void message.success(t('pages.auth.oauth2.bind.messages.bindSuccess'));
                    setUserAuthentication(res.data.token, res.data.expiresIn);
                    navigate(menuPathDashboard);
                } else {
                    void message.error(res.message || t('pages.auth.oauth2.bind.messages.bindFailed'));
                }
            })
            .catch(() => {
                void message.error(t('pages.auth.oauth2.bind.messages.bindRetry'));
            })
            .finally(() => {
                setIsSubmitting(false);
            });
    };

    return (
        <div className="space-y-6">
            <Card className="bg-blue-50 border-blue-200">
                <Space direction="vertical" size="middle" className="w-full">
                    <div className="text-sm text-gray-500">{t('pages.auth.oauth2.bind.currentUser.label')}</div>
                    <Space>
                        <Avatar
                            size={48}
                            src={currentUser.avatar}
                            icon={<UserOutlined />}
                        />
                        <div>
                            <div className="font-medium">{currentUser.nickname}</div>
                            <div className="text-sm text-gray-500">@{currentUser.username}</div>
                        </div>
                    </Space>
                </Space>
            </Card>

            <Button
                type="primary"
                size="large"
                loading={isSubmitting}
                className="w-full h-12 text-base font-semibold shadow-lg rounded-xl border-none"
                onClick={handleBind}
            >
                {t('pages.auth.oauth2.bind.currentUser.button')}
            </Button>
        </div>
    );
}

export function OAuth2CodePage() {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const loggedUser = useLoggedUser();

    const code = getQueryString('code');
    const state = getQueryString('state');

    const [isProcessing, setProcessing] = useState(false);
    const [isProcessed, setProcessed] = useState(false);
    const [userInfo, setUserInfo] = useState<OAuth2UserInfo | null>(null);
    const [activeTab, setActiveTab] = useState<string>('current');
    const isLoggedIn = loggedUser.hasAuthToken;

    if (!code || !state) {
        void message.error(t('pages.auth.oauth2.messages.invalidLoginInfo'));
        navigate(menuPathLogin);
        return null;
    }

    useEffect(() => {
        setActiveTab(isLoggedIn ? 'current' : 'register');
    }, [isLoggedIn]);

    const handleLogin = () => {
        if (isProcessing || isProcessed) return;

        setProcessing(true);

        loginByOAuth2Code(code, state)
            .then((res) => {
                if (res.data) {
                    if (isLoginResponse(res.data)) {
                        void message.success(t('pages.auth.oauth2.messages.success'));
                        setUserAuthentication(res.data.token, res.data.expiresIn);
                        navigate(menuPathDashboard);
                    } else {
                        setUserInfo(res.data as OAuth2UserInfo);
                    }
                } else {
                    void message.error(t('pages.auth.oauth2.messages.unknownError'));
                }
            })
            .catch(() => {
                void message.error(t('pages.auth.oauth2.messages.failed'));
            })
            .finally(() => {
                setProcessing(false);
                setProcessed(true);
            });
    }

    if (userInfo) {
        const tabItems = [
            ...(isLoggedIn && loggedUser.userProfile ? [{
                key: 'current',
                label: t('pages.auth.oauth2.bind.tabs.current'),
                children: <BindCurrentUserTab
                    userInfo={userInfo}
                    currentUser={{
                        nickname: loggedUser.userProfile!.nickname,
                        username: loggedUser.userProfile!.username!,
                        avatar: loggedUser.userProfile!.avatar
                    }}
                />
            }] : []),
            {
                key: 'register',
                label: t('pages.auth.oauth2.bind.tabs.register'),
                children: <RegisterTab userInfo={userInfo} />
            },
            {
                key: 'bind',
                label: t('pages.auth.oauth2.bind.tabs.bind'),
                children: <BindTab userInfo={userInfo} />
            }
        ];

        return (
            <AuthCardLayout
                title={t('pages.auth.oauth2.bind.title')}
                subtitle={t('pages.auth.oauth2.bind.subtitle')}
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
                                <PlatformIcon platform={getOAuthPlatformByName(userInfo.platform)!} />
                            </div>
                        </div>
                    </div>
                </Card>
                <Tabs
                    activeKey={activeTab}
                    onChange={(key) => setActiveTab(key)}
                    items={tabItems}
                    centered
                    className="w-full"
                />
            </AuthCardLayout>
        );
    }

    return (
        <AuthCardLayout
            title={t('pages.auth.oauth2.title')}
            subtitle={t('pages.auth.oauth2.subtitle')}
        >
            <Button
                type="primary"
                size="large"
                loading={isProcessing}
                disabled={isProcessed && !userInfo}
                className="w-full h-12 text-base font-semibold shadow-lg rounded-xl border-none active:scale-[0.98] transition-all"
                onClick={handleLogin}
            >
                {isProcessing ? t('pages.auth.oauth2.button.processing') : t('pages.auth.oauth2.button.confirm')}
            </Button>
        </AuthCardLayout>
    )
}
