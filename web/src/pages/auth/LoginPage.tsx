import {Avatar, Button, Checkbox, Divider, Form, Input, message, Space} from 'antd';
import {LockOutlined, MailOutlined, ShopOutlined} from '@ant-design/icons';
import {useEffect, useState} from 'react';
import {type NavigateFunction, useNavigate} from 'react-router-dom';
import {AuthCardLayout} from './AuthorizationPage.tsx';
import {login} from "@/api/auth.api.ts";
import {getUserAuthentication, setUserAuthentication} from "@/utils/token.utils.ts";
import {getQueryString} from "@/utils/url.utils.ts";
import {buildDocumentTitle} from "@/global/global-settings.ts";
import {menuPathDashboard, menuPathRegister, menuPathResetPassword} from "@/router";
import {OAuthLoginButton} from "../../components/OAuthLoginButton.tsx";
import {OAuthPlatform} from "@/types/oauth-account.types.ts";
import {getJoinedTenants} from "@/api/tenant.api.ts";
import type {UserTenantVO} from "@/types/tenant.types.ts";

interface LoginFormData {
    username: string,
    password: string,
    remember: boolean,
    agreement: boolean
}

function turnToRedirectUrl(navigate: NavigateFunction) {
    const redirectTo = getQueryString('redirectTo') || menuPathDashboard;
    navigate(redirectTo);
}

function JoinedTenantAuth({ username, password, joinedTenants }: {
    username: string,
    password: string,
    joinedTenants: UserTenantVO[]
}) {
    if (!joinedTenants || joinedTenants.length == 0) {
        return (
            <span>你不属于任何组织</span>
        )
    }

    const navigate = useNavigate();
    const [selectedTenant, setSelectedTenant] = useState<UserTenantVO | null>(null);
    const [isLoading, setIsLoading] = useState<boolean>(false);

    const skipLogin = () => {
        turnToRedirectUrl(navigate);
    }

    const loginWithSelectedTenant = () => {
        if (!selectedTenant || selectedTenant.tenantId === '0') {
            skipLogin();
        }

        setIsLoading(true);
        login(username, password, selectedTenant?.tenantId ?? undefined)
            .then((res) => {
                if (res.data) {
                    const loginData = res.data;
                    setUserAuthentication(loginData.token, loginData.expiresIn);
                    turnToRedirectUrl(navigate);
                } else {
                    void message.error('登录失败 未知错误');
                }
            })
            .catch((err) => {
                console.log(err);
                void message.error(`无法以 ${selectedTenant?.tenantName} 的组织身份登录`)
            })
            .finally(() => {
                setIsLoading(false);
            })
    }

    return (
        <div className="flex flex-col space-y-4">
            <Space orientation="vertical" size={8}>
                {[
                    ...[{
                        tenantId: 0,
                        tenantAvatar: null,
                        tenantName: '以非组织身份登录'
                    }] as unknown as UserTenantVO[],
                    ...joinedTenants
                ].map((tenant) => (
                    <div
                        key={tenant.tenantId}
                        className={`p-3 rounded-lg border cursor-pointer transition-colors ${
                            selectedTenant?.tenantId === tenant.tenantId
                                ? 'border-blue-500 bg-blue-50'
                                : 'border-gray-200 hover:border-gray-300'
                        }`}
                        onClick={() => setSelectedTenant(tenant)}
                    >
                        <div className="flex items-center gap-3">
                            <input
                                type="radio"
                                checked={selectedTenant?.tenantId === tenant.tenantId}
                                onChange={() => setSelectedTenant(tenant)}
                                className="cursor-pointer"
                            />
                            <Space orientation="horizontal" size={16}>
                                <Avatar
                                    src={tenant.tenantAvatar}
                                    icon={<ShopOutlined />} />
                                <div className="flex flex-col">
                                    <span>{tenant.tenantName}</span>
                                    <span className="text-[12px] text-gray-400">{username}</span>
                                </div>
                            </Space>
                        </div>
                    </div>
                ))}
            </Space>

            <div className="flex flex-row items-center space-x-4">
                <Button className="flex-1" loading={isLoading} size="large" onClick={skipLogin}>跳过</Button>
                <Button className="flex-1" loading={isLoading} type="primary" size="large" onClick={loginWithSelectedTenant}>
                    确认登录
                </Button>
            </div>
        </div>
    )
}

export function LoginPage() {
    const [loading, setLoading] = useState(false);
    const [agreedToTerms, setAgreedToTerms] = useState(false);
    const navigate = useNavigate();

    const [joinedTenants, setJoinedTenants] = useState<UserTenantVO[]>([]);
    const [loggedUsername, setLoggedUsername] = useState<string>("");
    const [loggedUserPassword, setLoggedUserPassword] = useState<string>("");

    useEffect(() => {
        document.title = buildDocumentTitle('登录')
    }, []);

    useEffect(() => {
        const auth = getUserAuthentication();
        if (auth && !auth.expired) {
            navigate(menuPathDashboard);
        }
    }, [navigate]);

    const onFinish = (values: LoginFormData) => {
        setLoading(true);

        if (values.remember) {
            localStorage.setItem('login_username', values.username);
        }

        login(values.username, values.password)
            .then((res) => {
                void message.success('登录成功');
                if (res.data) {
                    const loginData = res.data;
                    setUserAuthentication(loginData.token, loginData.expiresIn);
                    setLoggedUsername(values.username);
                    setLoggedUserPassword(values.password);

                    // Check joined tenants
                    getJoinedTenants()
                        .then((tenantsResult) => {
                            const joinedTenants = tenantsResult.data ?? []
                            if (joinedTenants && joinedTenants.length > 0) {
                                setJoinedTenants(joinedTenants);
                            } else {
                                turnToRedirectUrl(navigate);
                            }
                        })
                } else {
                    void message.error('登录失败 未知错误');
                }
            })
            .catch(() => {
                void message.error('登录失败');
            })
            .finally(() => {
                setLoading(false);
            })
    };

    return (
        <AuthCardLayout
            title="欢迎回来"
            subtitle="请输入您的凭据以访问控制台"
            footerText="还没有账号?"
            footerLink="现在注册"
            footerAction={() => navigate(menuPathRegister)}
        >
            {(joinedTenants && joinedTenants.length > 0) ? (
                <JoinedTenantAuth
                    username={loggedUsername}
                    password={loggedUserPassword}
                    joinedTenants={joinedTenants}
                />
            ) : (
                <>
                    <Form
                        name="login_form"
                        layout="vertical"
                        initialValues={{
                            username: localStorage.getItem('login_username') || '',
                            remember: true
                        }}
                        onFinish={onFinish}
                        size="large"
                        requiredMark={false}
                        autoComplete="off"
                    >
                        <Form.Item
                            name="username"
                            rules={[
                                {required: true, message: '请输入用户名或邮箱'},
                                {pattern: /^[a-zA-Z0-9_@.-]+$/, message: '只能包含英文字母、数字、下划线和横线或邮箱地址'},
                            ]}
                        >
                            <Input
                                prefix={<MailOutlined className="text-gray-400 mr-2"/>}
                                placeholder="用户名或邮箱"
                                className="rounded-xl"
                            />
                        </Form.Item>

                        <Form.Item
                            name="password"
                            rules={[{required: true, message: '请输入密码'}]}
                        >
                            <Input.Password
                                prefix={<LockOutlined className="text-gray-400 mr-2"/>}
                                placeholder="密码"
                                className="rounded-xl"
                            />
                        </Form.Item>

                        <div className="flex justify-between items-center mb-6">
                            <Form.Item name="remember" valuePropName="checked" noStyle>
                                <Checkbox className="text-xs text-gray-500">记住我</Checkbox>
                            </Form.Item>
                            <a
                                className="text-xs font-medium text-pink-400 hover:text-pink-400 transition-colors"
                                href={menuPathResetPassword}
                            >
                                忘记密码?
                            </a>
                        </div>

                        <Form.Item
                            name="agreement"
                            valuePropName="checked"
                            rules={[{required: true, message: '请阅读并同意服务条款和隐私政策'}]}
                            className="mb-6"
                        >
                            <Checkbox
                                className="text-xs text-gray-500"
                                onChange={(e) => setAgreedToTerms(e.target.checked)}
                            >
                                我已阅读并同意
                                <a href="/privacy" className="text-pink-400 hover:underline mx-1" target="_blank">隐私政策</a>
                                和
                                <a href="/terms" className="text-pink-400 hover:underline mx-1" target="_blank">服务条款</a>
                            </Checkbox>
                        </Form.Item>

                        <Form.Item className="mb-4">
                            <Button
                                type="primary"
                                htmlType="submit"
                                loading={loading}
                                className="w-full h-12 text-base font-semibold shadow-lg rounded-xl border-none active:scale-[0.98] transition-all"
                            >
                                立即登录
                            </Button>
                        </Form.Item>
                    </Form>

                    {/* Third Party Login */}
                    <div className="mt-8">
                        <Divider
                            plain
                            className="text-gray-400 text-[10px] uppercase tracking-widest"
                        >
                            或者通过以下方式
                        </Divider>
                        <div className="flex gap-4 mt-6">
                            <OAuthLoginButton
                                platform={OAuthPlatform.GITHUB}
                                agreedToTerms={agreedToTerms}
                            />

                            <OAuthLoginButton
                                platform={OAuthPlatform.GOOGLE}
                                agreedToTerms={agreedToTerms}
                            />

                            <OAuthLoginButton
                                platform={OAuthPlatform.OICQ}
                                agreedToTerms={agreedToTerms}
                            />
                        </div>
                    </div>
                </>
            )}

        </AuthCardLayout>
    );
}
