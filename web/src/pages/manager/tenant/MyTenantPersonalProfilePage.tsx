import {Avatar, Button, Card, DatePicker, Form, Input, message, Modal, Select, Space, Tabs, theme} from "antd";
import {
    ClockCircleOutlined,
    ExclamationCircleFilled,
    MailOutlined,
    PhoneOutlined,
    PlusOutlined,
    UserOutlined
} from "@ant-design/icons";
import {useEffect, useMemo, useState} from "react";
import {useSearchParams} from "react-router-dom";
import {useTranslation} from "react-i18next";
import dayjs from "dayjs";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {useSWRState} from "@/compositions/use-swr.ts";
import {useUserTenants} from "@/compositions/use-tenant.ts";
import {useLoggedUser} from "@/compositions/use-logged-user.ts";
import {
    getTenantOAuthAccounts,
    unbindTenantOAuthAccount
} from "@/api/tenant/tenant-oauth.api.ts";
import {
    getMyTenantUserProfile,
    upsertMyTenantUserProfile,
    type UpsertMyTenantUserProfileDTO
} from "@/api/tenant/tenant-user-profile.api.ts";
import type {TenantOAuthAccount} from "@/types/tenant/tenant-oauth.types.ts";
import {OAuthPlatform, OAuthBindingScope} from "@/types/user/oauth-account.types.ts";
import {Gender} from "@/types/common/gender.types.ts";
import {getGender} from "@/i18n/enum-helpers.ts";
import {PlatformIcon} from "@/components/PlatformIcon.tsx";
import {redirectToOAuthBind} from "@/utils/oauth2.ts";
import {formatTimestamp} from "@/utils/datetime.utils.ts";

const TAB_KEYS = {
    INFO: 'info',
    OAUTH: 'oauth'
} as const;

type TabKey = typeof TAB_KEYS[keyof typeof TAB_KEYS];

interface ProfileFormValues {
    name: string;
    phone: string;
    nickname?: string;
    email?: string;
    bio?: string;
    gender?: Gender;
    birthday?: dayjs.Dayjs;
    timezone?: string;
    locale?: string;
}

const TenantProfileInfo = () => {
    const { t } = useTranslation();
    const [form] = Form.useForm<ProfileFormValues>();

    const [profile, , isProfileLoading, reloadProfile] = useSWRState(
        'getMyTenantUserProfile',
        getMyTenantUserProfile,
        () => void message.error(t('pages.tenantPersonalProfile.info.loadFailed'))
    );

    useEffect(() => {
        if (profile) {
            form.setFieldsValue({
                name: profile.name,
                phone: profile.phone,
                nickname: profile.nickname ?? undefined,
                email: profile.email ?? undefined,
                bio: profile.bio ?? undefined,
                gender: profile.gender ?? undefined,
                birthday: profile.birthday ? dayjs(Number(profile.birthday)) : undefined,
                timezone: profile.timezone ?? undefined,
                locale: profile.locale ?? undefined,
            });
        }
    }, [profile, form]);

    const [saving, setSaving] = useState(false);
    const handleSubmit = async (values: ProfileFormValues) => {
        setSaving(true);
        try {
            // name (real name) is write-once on join; never sent on update.
            const dto: UpsertMyTenantUserProfileDTO = {
                phone: values.phone,
                nickname: values.nickname,
                email: values.email,
                bio: values.bio,
                gender: values.gender,
                birthday: values.birthday ? String(values.birthday.valueOf()) : undefined,
                timezone: values.timezone,
                locale: values.locale,
            };
            await upsertMyTenantUserProfile(dto);
            void message.success(t('pages.tenantPersonalProfile.info.saveSuccess'));
            await reloadProfile();
        } catch {
            void message.error(t('pages.tenantPersonalProfile.info.saveFailed'));
        } finally {
            setSaving(false);
        }
    };

    const genderOptions = useMemo(() => [
        Gender.UNSPECIFIED,
        Gender.MALE,
        Gender.FEMALE,
        Gender.OTHER,
    ].map(value => ({ value, label: getGender(value) })), []);

    return (
        <div className="py-4">
            <Form
                form={form}
                layout="vertical"
                onFinish={handleSubmit}
                disabled={isProfileLoading}
            >
                <div className="grid grid-cols-1 md:grid-cols-2 gap-x-6">
                    <Form.Item
                        label={t('pages.tenantPersonalProfile.info.name')}
                        name="name"
                        extra={<p className="mt-2 text-gray-400">{t('pages.tenantPersonalProfile.info.nameHint')}</p>}
                    >
                        <Input className="rounded-xl py-2" disabled />
                    </Form.Item>
                    <Form.Item
                        label={t('pages.tenantPersonalProfile.info.phone')}
                        name="phone"
                        rules={[
                            { required: true, message: t('pages.tenantPersonalProfile.info.phoneRequired') },
                            { max: 32 }
                        ]}
                    >
                        <Input className="rounded-xl py-2" />
                    </Form.Item>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-x-6">
                    <Form.Item
                        label={t('pages.tenantPersonalProfile.info.nickname')}
                        name="nickname"
                        rules={[{ max: 32 }]}
                        extra={<p className="mt-2 text-gray-400">{t('pages.tenantPersonalProfile.info.nicknameHint')}</p>}
                    >
                        <Input className="rounded-xl py-2" />
                    </Form.Item>
                    <Form.Item
                        label={t('pages.tenantPersonalProfile.info.email')}
                        name="email"
                        rules={[
                            { type: 'email', message: t('pages.tenantPersonalProfile.info.invalidEmail') },
                            { max: 256 }
                        ]}
                        extra={<p className="mt-2 text-gray-400">{t('pages.tenantPersonalProfile.info.emailHint')}</p>}
                    >
                        <Input className="rounded-xl py-2" />
                    </Form.Item>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-x-6">
                    <Form.Item
                        label={t('pages.tenantPersonalProfile.info.gender')}
                        name="gender"
                    >
                        <Select
                            className="rounded-xl"
                            options={genderOptions}
                            allowClear
                        />
                    </Form.Item>
                    <Form.Item
                        label={t('pages.tenantPersonalProfile.info.birthday')}
                        name="birthday"
                    >
                        <DatePicker className="rounded-xl py-2 w-full" />
                    </Form.Item>
                    <Form.Item
                        label={t('pages.tenantPersonalProfile.info.locale')}
                        name="locale"
                        rules={[{ max: 16 }]}
                    >
                        <Input className="rounded-xl py-2" placeholder="zh-CN" />
                    </Form.Item>
                </div>

                <Form.Item
                    label={t('pages.tenantPersonalProfile.info.timezone')}
                    name="timezone"
                    rules={[{ max: 64 }]}
                >
                    <Input className="rounded-xl py-2" placeholder="Asia/Shanghai" />
                </Form.Item>

                <Form.Item
                    label={t('pages.tenantPersonalProfile.info.bio')}
                    name="bio"
                    rules={[{ max: 512 }]}
                >
                    <Input.TextArea className="rounded-xl" rows={4} />
                </Form.Item>

                <Button
                    type="primary"
                    size="large"
                    className="rounded-xl px-8 h-auto py-2"
                    loading={saving}
                    onClick={() => { form.submit() }}
                >
                    {t('pages.tenantPersonalProfile.info.save')}
                </Button>
            </Form>
        </div>
    );
};

const TenantOAuthBindings = () => {
    const { t } = useTranslation();
    const [modal, contextHolder] = Modal.useModal();

    const [accounts, , , reloadAccounts] = useSWRState('/getTenantOAuthAccounts', getTenantOAuthAccounts);

    const allPlatforms = useMemo(() => [OAuthPlatform.GITHUB, OAuthPlatform.GOOGLE, OAuthPlatform.OICQ], []);

    const handleBind = (platform: OAuthPlatform) => {
        redirectToOAuthBind(platform, OAuthBindingScope.TENANT);
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

const { useToken } = theme;

function TenantPersonalProfileCard() {
    const { t } = useTranslation();
    const { token } = useToken();
    const loggedUser = useLoggedUser();

    const [profile] = useSWRState(
        'getMyTenantUserProfile',
        getMyTenantUserProfile,
    );

    const displayName = profile?.nickname ?? loggedUser.userProfile?.nickname ?? '';
    const realName = profile?.name ?? '';
    const email = profile?.email ?? loggedUser.userProfile?.email;
    const phone = profile?.phone ?? '';
    const joinedTime = profile ? Number(profile.createdTime) : null;

    return (
        <Card className="rounded-2xl shadow-sm border-none overflow-hidden">
            <div className="h-24 bg-gradient-to-r from-emerald-500 to-teal-600 -m-6 mb-0 dark:from-slate-800 dark:via-emerald-900/40 dark:to-slate-800"></div>
            <div className="relative pt-0 px-6 pb-6">
                <div className="flex justify-center -mt-12 mb-4 relative">
                    <Avatar
                        size={100}
                        className="rounded-3xl border-4 shadow-md bg-black/50"
                        icon={<UserOutlined />}
                        src={loggedUser.userProfile?.avatar ?? undefined}
                        style={{ borderColor: token.colorBorderSecondary }}
                    />
                </div>
                <div className="text-center mb-6">
                    <h2 className="text-xl font-bold text-slate-800">{displayName}</h2>
                    {realName && <p className="text-slate-400 text-sm italic">{realName}</p>}
                </div>
                <div className="space-y-4">
                    <div className="flex items-center text-slate-600 text-sm">
                        <MailOutlined className="mr-3 text-slate-300" />
                        {email || t('pages.tenantPersonalProfile.card.unboundEmail')}
                    </div>
                    <div className="flex items-center text-slate-600 text-sm">
                        <PhoneOutlined className="mr-3 text-slate-300" />
                        {phone || t('pages.tenantPersonalProfile.card.unboundPhone')}
                    </div>
                    {joinedTime !== null && (
                        <div className="flex items-center text-slate-600 text-sm">
                            <ClockCircleOutlined className="mr-3 text-slate-300" />
                            {t('pages.tenantPersonalProfile.card.joinedAt', { time: formatTimestamp(joinedTime) })}
                        </div>
                    )}
                </div>
            </div>
        </Card>
    );
}

export default function MyTenantPersonalProfilePage() {
    const { t } = useTranslation();
    const { currentTenant } = useUserTenants();

    const [searchParams, setSearchParams] = useSearchParams();
    const activeTab = (searchParams.get('tab') as TabKey) || TAB_KEYS.INFO;
    const handleTabChange = (key: string) => {
        setSearchParams({ tab: key });
    };

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
                            activeKey={activeTab}
                            className="modern-tabs"
                            items={[
                                {
                                    key: TAB_KEYS.INFO,
                                    label: <span className="px-2 font-medium">{t('pages.tenantPersonalProfile.tabs.info')}</span>,
                                    children: <TenantProfileInfo />,
                                },
                                {
                                    key: TAB_KEYS.OAUTH,
                                    label: <span className="px-2 font-medium">{t('pages.tenantPersonalProfile.tabs.oauth')}</span>,
                                    children: currentTenant ? <TenantOAuthBindings /> : null,
                                },
                            ]}
                            onChange={handleTabChange}
                        />
                    </Card>
                </div>
            </div>
        </>
    );
}
