import {Avatar, Button, Checkbox, Divider, Form, Input, message, Space, theme} from 'antd';
import {LockOutlined, MailOutlined, ShopOutlined} from '@ant-design/icons';
import {useEffect, useState} from 'react';
import {type NavigateFunction, useNavigate} from 'react-router-dom';
import {useTranslation} from 'react-i18next';
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
import {TenantMemberStatus} from "@/types/tenant-member.types.ts";

interface LoginFormData {
    username: string,
    password: string,
    remember: boolean,
    agreement: boolean
}

function turnToRedirectUrl(navigate: NavigateFunction) {
    const redirectTo = getQueryString('redirectTo') || menuPathDashboard;
    if (redirectTo.startsWith("/")) {
        navigate(redirectTo);
    } else {
        window.location.href = redirectTo;
    }
}

function JoinedTenantAuth({ username, password, joinedTenants }: {
    username: string,
    password: string,
    joinedTenants: UserTenantVO[]
}) {
    const { t } = useTranslation();
    const { token } = theme.useToken();

    if (!joinedTenants || joinedTenants.length == 0) {
        return (
            <span>{t('pages.auth.login.joinedTenant.noTenant')}</span>
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
                    void message.error(t('pages.auth.login.messages.unknownError'));
                }
            })
            .catch((err) => {
                console.log(err);
                void message.error(t('pages.auth.login.messages.failed'))
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
                        tenantName: t('pages.auth.login.joinedTenant.loginAsNonTenant'),
                        memberStatus: TenantMemberStatus.ACTIVE,
                        authenticated: true
                    }] as unknown as UserTenantVO[],
                    ...joinedTenants
                ].filter((it) => it.memberStatus === TenantMemberStatus.ACTIVE).map((tenant) => (
                    <div
                        key={tenant.tenantId}
                        className={`p-3 rounded-lg border cursor-pointer transition-colors ${
                            selectedTenant?.tenantId === tenant.tenantId
                                ? ''
                                : 'border-gray-200 hover:border-gray-300'
                        }`}
                        style={selectedTenant?.tenantId === tenant.tenantId ? {
                            borderColor: token.colorPrimary,
                            backgroundColor: token.colorPrimaryBg
                        } : {}}
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
                <Button className="flex-1" loading={isLoading} size="large" onClick={skipLogin}>{t('pages.auth.login.joinedTenant.skip')}</Button>
                <Button className="flex-1" loading={isLoading} type="primary" size="large" onClick={loginWithSelectedTenant}>
                    {t('pages.auth.login.joinedTenant.confirm')}
                </Button>
            </div>
        </div>
    )
}

export function LoginPage() {
    const { t } = useTranslation();
    const [loading, setLoading] = useState(false);
    const [agreedToTerms, setAgreedToTerms] = useState(false);
    const navigate = useNavigate();

    const [joinedTenants, setJoinedTenants] = useState<UserTenantVO[]>([]);
    const [loggedUsername, setLoggedUsername] = useState<string>("");
    const [loggedUserPassword, setLoggedUserPassword] = useState<string>("");

    useEffect(() => {
        document.title = buildDocumentTitle(t('pages.auth.login.title'))
    }, [t]);

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
                void message.success(t('pages.auth.login.messages.success'));
                if (res.data) {
                    const loginData = res.data;
                    setUserAuthentication(loginData.token, loginData.expiresIn);
                    setLoggedUsername(values.username);
                    setLoggedUserPassword(values.password);

                    // Check joined tenants
                    // @ts-ignore
                    getJoinedTenants()
                        .then((tenantsResult: { data?: UserTenantVO[] | null }) => {
                            const joinedTenants = tenantsResult.data ?? []
                            if (joinedTenants && joinedTenants.length > 0) {
                                setJoinedTenants(joinedTenants);
                            } else {
                                turnToRedirectUrl(navigate);
                            }
                        })
                } else {
                    void message.error(t('pages.auth.login.messages.unknownError'));
                }
            })
            .catch(() => {
                void message.error(t('pages.auth.login.messages.failed'));
            })
            .finally(() => {
                setLoading(false);
            })
    };

    return (
        <AuthCardLayout
            title={t('pages.auth.login.title')}
            subtitle={t('pages.auth.login.subtitle')}
            footerText={t('pages.auth.login.footerText')}
            footerLink={t('pages.auth.login.footerLink')}
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
                                {required: true, message: t('pages.auth.login.form.username.required')},
                                {pattern: /^[a-zA-Z0-9_@.-]+$/, message: t('pages.auth.login.form.username.pattern')},
                            ]}
                        >
                            <Input
                                prefix={<MailOutlined className="text-gray-400 mr-2"/>}
                                placeholder={t('pages.auth.login.form.username.placeholder')}
                                className="rounded-xl"
                            />
                        </Form.Item>

                        <Form.Item
                            name="password"
                            rules={[{required: true, message: t('pages.auth.login.form.password.required')}]}
                        >
                            <Input.Password
                                prefix={<LockOutlined className="text-gray-400 mr-2"/>}
                                placeholder={t('pages.auth.login.form.password.placeholder')}
                                className="rounded-xl"
                            />
                        </Form.Item>

                        <div className="flex justify-between items-center mb-6">
                            <Form.Item name="remember" valuePropName="checked" noStyle>
                                <Checkbox className="text-xs text-gray-500">{t('pages.auth.login.form.remember')}</Checkbox>
                            </Form.Item>
                            <a
                                className="text-xs font-medium text-pink-400 hover:text-pink-400 transition-colors"
                                href={menuPathResetPassword}
                            >
                                {t('pages.auth.login.form.forgotPassword')}
                            </a>
                        </div>

                        <Form.Item
                            name="agreement"
                            valuePropName="checked"
                            rules={[{required: true, message: t('pages.auth.login.form.agreement.required')}]}
                            className="mb-6"
                        >
                            <Checkbox
                                className="text-xs text-gray-500"
                                onChange={(e) => setAgreedToTerms(e.target.checked)}
                            >
                                {t('pages.auth.login.form.agreement.text')}
                                <a href="/privacy" className="text-pink-400 hover:underline mx-1" target="_blank">{t('pages.auth.login.form.agreement.privacyPolicy')}</a>
                                {t('pages.auth.login.form.agreement.and')}
                                <a href="/terms" className="text-pink-400 hover:underline mx-1" target="_blank">{t('pages.auth.login.form.agreement.termsOfService')}</a>
                            </Checkbox>
                        </Form.Item>

                        <Form.Item className="mb-4">
                            <Button
                                type="primary"
                                htmlType="submit"
                                loading={loading}
                                className="w-full h-12 text-base font-semibold shadow-lg rounded-xl border-none active:scale-[0.98] transition-all"
                            >
                                {t('pages.auth.login.form.submit')}
                            </Button>
                        </Form.Item>
                    </Form>

                    {/* Third Party Login */}
                    <div className="mt-8">
                        <Divider
                            plain
                            className="text-gray-400 text-[10px] uppercase tracking-widest"
                        >
                            {t('pages.auth.login.divider')}
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
