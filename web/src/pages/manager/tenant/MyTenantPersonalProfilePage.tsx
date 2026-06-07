import {Avatar, Button, Card, message, Modal, Space, Tabs, theme} from "antd";
import {
    ClockCircleOutlined,
    ExclamationCircleFilled,
    MailOutlined,
    PlusOutlined,
    UserOutlined
} from "@ant-design/icons";
import {useMemo} from "react";
import {useTranslation} from "react-i18next";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {useSWRState} from "@/compositions/use-swr.ts";
import {useUserTenants} from "@/compositions/use-tenant.ts";
import {
    getTenantOAuthAccounts,
    unbindTenantOAuthAccount
} from "@/api/tenant/tenant-oauth.api.ts";
import type {TenantOAuthAccount} from "@/types/tenant/tenant-oauth.types.ts";
import {OAuthPlatform} from "@/types/user/oauth-account.types.ts";
import {PlatformIcon} from "@/components/PlatformIcon.tsx";
import {getOAuth2LoginUrl} from "@/utils/oauth2.ts";
import {PLATFORM_REGISTRATION_ID_MAP} from "@/global/constants.ts";

// Key under which a tenant-scoped OAuth bind intent is stashed across the OAuth redirect.
// Read back by OAuth2CodePage after the provider callback.
export const TENANT_OAUTH_BIND_INTENT_KEY = 'tenant-oauth-bind-intent';

const { useToken } = theme;

const TenantOAuthBindings = () => {
    const { t } = useTranslation();
    const [modal, contextHolder] = Modal.useModal();

    const [accounts, , , reloadAccounts] = useSWRState('/getTenantOAuthAccounts', getTenantOAuthAccounts);

    const allPlatforms = useMemo(() => [OAuthPlatform.GITHUB, OAuthPlatform.GOOGLE, OAuthPlatform.OICQ], []);

    const handleBind = (platform: OAuthPlatform) => {
        // Persist the intent so OAuth2CodePage performs a tenant bind instead of a login after callback.
        sessionStorage.setItem(TENANT_OAUTH_BIND_INTENT_KEY, '1');
        window.location.href = getOAuth2LoginUrl(PLATFORM_REGISTRATION_ID_MAP[platform]);
    };

    const handleUnbind = (account: TenantOAuthAccount) => {
        modal.confirm({
            title: t('pages.tenantPersonalProfile.oauth.unbindTitle'),
            icon: <ExclamationCircleFilled />,
            content: t('pages.tenantPersonalProfile.oauth.unbindConfirm', { nickname: account.nickname ?? '' }),
            onOk() {
                unbindTenantOAuthAccount({ oauthAccountId: account.id })
                    .then(() => {
                        void message.success(t('pages.tenantPersonalProfile.oauth.unbindSuccess'));
                        void reloadAccounts();
                    })
                    .catch(() => {
                        void message.warning(t('pages.tenantPersonalProfile.oauth.unbindFailed'));
                    });
            },
        });
    };

    return (
        <div className="space-y-4">
            {(accounts ?? []).map((account) => (
                <div key={account.id} className="flex flex-row items-center justify-between py-3 border-b border-gray-100 last:border-b-0">
                    <Space orientation="horizontal" size={12}>
                        <Avatar size={40} icon={<UserOutlined />} src={account.avatar ?? undefined} />
                        <Space orientation="vertical" size={0}>
                            <span className="font-medium">{account.nickname}</span>
                            <PlatformIcon platform={account.platformId as OAuthPlatform} />
                        </Space>
                    </Space>
                    <Button type="link" className="font-medium text-red-500" onClick={() => handleUnbind(account)}>
                        {t('pages.tenantPersonalProfile.oauth.unbind')}
                    </Button>
                </div>
            ))}

            <div className="pt-4">
                <div className="text-sm text-gray-500 mb-3">{t('pages.tenantPersonalProfile.oauth.availablePlatforms')}</div>
                <div className="flex flex-wrap gap-3">
                    {allPlatforms.map((platform) => (
                        <Button
                            key={platform}
                            type="dashed"
                            className="flex items-center gap-2 px-4 py-2 h-auto"
                            onClick={() => handleBind(platform)}
                        >
                            <PlusOutlined />
                            <PlatformIcon platform={platform} />
                        </Button>
                    ))}
                </div>
            </div>

            {contextHolder}
        </div>
    );
};

// Placeholder profile card. Per current scope this shows hardcoded sample data (English,
// non-i18n) and makes no backend calls — only the OAuth tab is wired to real endpoints.
function TenantPersonalProfileCard() {
    const { token } = useToken();

    return (
        <Card className="rounded-2xl shadow-sm border-none overflow-hidden">
            <div className="h-24 bg-gradient-to-r from-emerald-500 to-teal-600 -m-6 mb-0 dark:from-slate-800 dark:via-emerald-900/40 dark:to-slate-800"></div>
            <div className="relative pt-0 px-6 pb-6">
                <div className="flex justify-center -mt-12 mb-4 relative">
                    <Avatar
                        size={100}
                        className="rounded-3xl border-4 shadow-md bg-black/50"
                        icon={<UserOutlined />}
                        style={{ borderColor: token.colorBorderSecondary }}
                    />
                </div>
                <div className="text-center mb-6">
                    <h2 className="text-xl font-bold text-slate-800">Sample Member</h2>
                    <p className="text-slate-400 text-sm italic">@sample_member</p>
                </div>
                <div className="space-y-4">
                    <div className="flex items-center text-slate-600 text-sm">
                        <MailOutlined className="mr-3 text-slate-300" />
                        sample.member@example.com
                    </div>
                    <div className="flex items-center text-slate-600 text-sm">
                        <ClockCircleOutlined className="mr-3 text-slate-300" />
                        Joined 2026-01-01 00:00:00
                    </div>
                </div>
            </div>
        </Card>
    );
}

export default function MyTenantPersonalProfilePage() {
    const { t } = useTranslation();
    const { currentTenant } = useUserTenants();

    return (
        <>
            <ActionBarComponent
                title={t('pages.tenantPersonalProfile.title')}
                subtitle={t('pages.tenantPersonalProfile.subtitle')}
            />

            <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
                <div className="lg:col-span-4">
                    <TenantPersonalProfileCard />
                </div>

                <div className="lg:col-span-8">
                    <Card className="rounded-2xl shadow-sm border-none min-h-[500px]">
                        <Tabs
                            activeKey="oauth"
                            className="modern-tabs"
                            items={[
                                {
                                    key: 'oauth',
                                    label: <span className="px-2 font-medium">{t('pages.tenantPersonalProfile.tabs.oauth')}</span>,
                                    children: currentTenant ? <TenantOAuthBindings /> : null,
                                },
                            ]}
                        />
                    </Card>
                </div>
            </div>
        </>
    );
}

