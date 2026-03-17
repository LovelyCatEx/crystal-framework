import {useUserTenants} from "@/compositions/use-tenant.ts";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {Avatar, Card, Col, Divider, Empty, Row, Spin, Tag, theme, Tooltip, Typography} from "antd";
import {
    CalendarOutlined,
    ClockCircleOutlined,
    HomeOutlined,
    MailOutlined,
    PhoneOutlined,
    ShopOutlined,
    UserOutlined
} from "@ant-design/icons";
import {useTranslation} from "react-i18next";
import {getTenantStatus, getTenantMemberStatus} from "@/i18n/enum-helpers.ts";
import {formatTimestamp} from "@/utils/datetime.utils.ts";
import {useEffect, useState} from "react";
import {getUserProfile} from "@/api/user.api.ts";
import type {UserProfileVO} from "@/types/user.types.ts";

const { Title, Text } = Typography;
const { useToken } = theme;

function formatValue(value: string | null | undefined, t: (key: string) => string): string {
    return value ?? t('pages.myTenantDashboard.contact.notSet');
}

function getExpireStatus(expiresTime: string | null | undefined): { isExpired: boolean; isNearExpire: boolean; daysLeft: number } {
    if (!expiresTime) return { isExpired: false, isNearExpire: false, daysLeft: 0 };
    const expireDate = new Date(Number(expiresTime));
    const now = new Date();
    const daysLeft = Math.ceil((expireDate.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));
    return {
        isExpired: daysLeft < 0,
        isNearExpire: daysLeft >= 0 && daysLeft <= 30,
        daysLeft
    };
}

export function MyTenantDashboard() {
    const { t } = useTranslation();
    const { token } = useToken();
    const { joinedTenants, isJoinedTenantsLoading, currentTenant, currentTenantProfile } = useUserTenants();
    const [profile, , isProfileLoading] = currentTenantProfile;
    const [owner, setOwner] = useState<UserProfileVO | null>(null);
    const [isOwnerLoading, setIsOwnerLoading] = useState(false);

    useEffect(() => {
        if (profile?.ownerUserId) {
            setIsOwnerLoading(true);
            getUserProfile(profile.ownerUserId)
                .then((res) => setOwner(res.data))
                .catch(() => setOwner(null))
                .finally(() => setIsOwnerLoading(false));
        } else {
            setOwner(null);
        }
    }, [profile?.ownerUserId]);

    if (isJoinedTenantsLoading || isProfileLoading) {
        return (
            <>
                <ActionBarComponent title={t('pages.myTenantDashboard.title')} subtitle={t('pages.myTenantDashboard.subtitle')} />
                <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 256 }}>
                    <Spin size="large" />
                </div>
            </>
        );
    }

    if (!joinedTenants || joinedTenants.length === 0) {
        return (
            <>
                <ActionBarComponent title={t('pages.myTenantDashboard.title')} subtitle={t('pages.myTenantDashboard.subtitle')} />
                <Empty
                    description={t('pages.myTenantDashboard.noTenants')}
                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                />
            </>
        );
    }

    const statusColors: Record<number, string> = {
        0: 'orange',
        1: 'green',
        2: 'red'
    };
    const statusColor = profile ? statusColors[profile.status] : null;
    const expireStatus = profile ? getExpireStatus(profile.expiresTime) : null;

    return (
        <>
            <ActionBarComponent title={currentTenant?.tenantName || t('pages.myTenantDashboard.title')} subtitle={t('pages.myTenantDashboard.subtitle')} />

            {profile && (
                <Row gutter={[24, 24]} style={{ marginBottom: 24 }}>
                    <Col xs={24} lg={8}>
                        <Card className="rounded-2xl shadow-sm border-none">
                            <div className="text-center mb-6">
                                <Avatar
                                    size={100}
                                    src={profile.icon}
                                    icon={<ShopOutlined />}
                                    className="border-4 mb-4"
                                    style={{ borderColor: token.colorBorderSecondary }}
                                />
                                <Title level={4} className="!mb-2">{profile.name}</Title>
                                {profile && (
                                    <Tag color={statusColor || 'default'} className="text-sm px-3 py-1">
                                        {getTenantStatus(profile.status)}
                                    </Tag>
                                )}
                                {profile.description && (
                                    <Text type="secondary" className="block mt-3 px-4">
                                        {profile.description.length > 256
                                            ? profile.description.slice(0, 256) + '...'
                                            : profile.description}
                                    </Text>
                                )}
                            </div>

                            <Divider className="my-4" />

                            <div className="grid grid-cols-2 gap-4 text-center mb-4">
                                <div className="bg-blue-50 rounded-xl p-4 dark:bg-blue-950/30">
                                    <CalendarOutlined className="text-blue-500 text-lg mb-2 dark:text-blue-300/70" />
                                    <div className="text-xs text-slate-400 mb-1 dark:text-slate-500">{t('pages.myTenantDashboard.subscription.subscribedTime')}</div>
                                    <div className="text-sm font-medium text-slate-700 dark:text-slate-300">
                                        {profile.subscribedTime ? formatTimestamp(Number(profile.subscribedTime), 'YYYY-MM-DD') : '-'}
                                    </div>
                                </div>
                                <Tooltip title={expireStatus && profile.expiresTime ? (expireStatus.isExpired ? t('pages.myTenantDashboard.subscription.expired') : t('pages.myTenantDashboard.subscription.daysLeft', { days: expireStatus.daysLeft })) : ''}>
                                    <div className={`rounded-xl p-4 cursor-pointer ${
                                        expireStatus?.isExpired 
                                            ? 'bg-red-50 dark:bg-red-950/30' 
                                            : expireStatus?.isNearExpire 
                                                ? 'bg-orange-50 dark:bg-orange-950/30' 
                                                : 'bg-green-50 dark:bg-green-950/30'
                                    }`}>
                                        <ClockCircleOutlined className={`text-lg mb-2 ${
                                            expireStatus?.isExpired 
                                                ? 'text-red-500 dark:text-red-300/70' 
                                                : expireStatus?.isNearExpire 
                                                    ? 'text-orange-500 dark:text-orange-300/70' 
                                                    : 'text-green-500 dark:text-green-300/70'
                                        }`} />
                                        <div className="text-xs text-slate-400 mb-1 dark:text-slate-500">{t('pages.myTenantDashboard.subscription.expiresTime')}</div>
                                        <div className={`text-sm font-medium ${
                                            expireStatus?.isExpired 
                                                ? 'text-red-600 dark:text-red-300/80' 
                                                : expireStatus?.isNearExpire 
                                                    ? 'text-orange-600 dark:text-orange-300/80' 
                                                    : 'text-green-600 dark:text-green-300/80'
                                        }`}>
                                            {profile.expiresTime ? formatTimestamp(Number(profile.expiresTime), 'YYYY-MM-DD') : '-'}
                                        </div>
                                    </div>
                                </Tooltip>
                            </div>

                            <div className="bg-gray-50 rounded-xl p-4 dark:bg-slate-800/40">
                                <div className="flex items-center justify-center gap-2 mb-2">
                                    <span className="text-xs text-slate-400 dark:text-slate-500">{t('pages.myTenantDashboard.owner.title')}</span>
                                </div>
                                <div className="flex items-center justify-center gap-3">
                                    {isOwnerLoading ? (
                                        <Spin size="small" />
                                    ) : owner ? (
                                        <>
                                            <Avatar size={36} src={owner.avatar} icon={<UserOutlined />} />
                                            <span className="text-sm font-medium text-slate-700 dark:text-slate-300">
                                                {formatValue(owner.nickname, t)}
                                            </span>
                                        </>
                                    ) : (
                                        <>
                                            <Avatar size={36} icon={<UserOutlined />} />
                                            <span className="text-sm text-slate-500 dark:text-slate-400">{formatValue(profile.ownerUserId, t)}</span>
                                        </>
                                    )}
                                </div>
                            </div>
                        </Card>
                    </Col>

                    <Col xs={24} lg={16}>
                        <Card className="rounded-2xl shadow-sm border-none h-full">
                            <div className="mb-6">
                                <Title level={5} className="!mb-1">{t('pages.myTenantDashboard.basicInfo')}</Title>
                                <Text type="secondary">{t('pages.myTenantDashboard.basicInfoDesc')}</Text>
                            </div>

                            <div className="mb-6">
                                <div className="flex items-center gap-2 mb-4">
                                    <div className="w-1 h-5 bg-gradient-to-b from-blue-400 to-blue-600 rounded-full"></div>
                                    <span className="text-sm font-medium text-slate-700 dark:text-slate-300">{t('pages.myTenantDashboard.basicInfo')}</span>
                                </div>
                                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                    <div className="group bg-slate-50 hover:bg-blue-50 rounded-xl p-4 transition-all duration-300 border border-slate-100 hover:border-blue-200 dark:bg-slate-800/40 dark:border-slate-700/50 dark:hover:border-blue-800/50">
                                        <div className="flex items-center gap-2 text-slate-400 text-xs mb-2 dark:text-slate-500">
                                            <div className="w-6 h-6 rounded-lg bg-white flex items-center justify-center shadow-sm dark:bg-slate-700/50">
                                                <ShopOutlined className="text-blue-500 text-xs dark:text-blue-300/70" />
                                            </div>
                                            {t('pages.myTenantDashboard.tenantId')}
                                        </div>
                                        <Text copyable className="text-sm font-medium text-slate-700 font-mono dark:text-slate-300">
                                            {formatValue(profile.tenantId, t)}
                                        </Text>
                                    </div>

                                    <div className="group bg-slate-50 hover:bg-green-50 rounded-xl p-4 transition-all duration-300 border border-slate-100 hover:border-green-200 dark:bg-slate-800/40 dark:border-slate-700/50 dark:hover:border-green-800/50">
                                        <div className="flex items-center gap-2 text-slate-400 text-xs mb-2 dark:text-slate-500">
                                            <div className="w-6 h-6 rounded-lg bg-white flex items-center justify-center shadow-sm dark:bg-slate-700/50">
                                                <span className="w-2 h-2 rounded-full bg-green-500 dark:bg-green-300/70"></span>
                                            </div>
                                            {t('pages.myTenantDashboard.member.status')}
                                        </div>
                                        <div className="text-sm font-medium">
                                            {profile ? (
                                                <Tag color={statusColor || 'default'} className="!text-sm !px-3 !py-0.5">
                                                    {getTenantStatus(profile.status)}
                                                </Tag>
                                            ) : '-'}
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <Divider className="my-4" />

                            <div className="mb-6">
                                <div className="flex items-center gap-2 mb-4">
                                    <div className="w-1 h-5 bg-gradient-to-b from-purple-400 to-purple-600 rounded-full"></div>
                                    <span className="text-sm font-medium text-slate-700 dark:text-slate-300">{t('pages.myTenantDashboard.contact.title')}</span>
                                </div>
                                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                    <div className="group bg-slate-50 hover:bg-purple-50 rounded-xl p-4 transition-all duration-300 border border-slate-100 hover:border-purple-200 dark:bg-slate-800/40 dark:border-slate-700/50 dark:hover:border-purple-600/50">
                                        <div className="flex items-center gap-2 text-slate-400 text-xs mb-2 dark:text-slate-500">
                                            <div className="w-6 h-6 rounded-lg bg-white flex items-center justify-center shadow-sm dark:bg-slate-700/50">
                                                <UserOutlined className="text-purple-500 text-xs dark:text-purple-300/70" />
                                            </div>
                                            {t('pages.myTenantDashboard.contact.name')}
                                        </div>
                                        <div className="text-sm font-medium text-slate-700 dark:text-slate-300">
                                            {formatValue(profile.contactName, t)}
                                        </div>
                                    </div>

                                    <div className="group bg-slate-50 hover:bg-purple-50 rounded-xl p-4 transition-all duration-300 border border-slate-100 hover:border-purple-200 dark:bg-slate-800/40 dark:border-slate-700/50 dark:hover:border-purple-600/50">
                                        <div className="flex items-center gap-2 text-slate-400 text-xs mb-2 dark:text-slate-500">
                                            <div className="w-6 h-6 rounded-lg bg-white flex items-center justify-center shadow-sm dark:bg-slate-700/50">
                                                <MailOutlined className="text-purple-500 text-xs dark:text-purple-300/70" />
                                            </div>
                                            {t('pages.myTenantDashboard.contact.email')}
                                        </div>
                                        <Text copyable className="text-sm font-medium text-slate-700 dark:text-slate-300">
                                            {formatValue(profile.contactEmail, t)}
                                        </Text>
                                    </div>

                                    <div className="group bg-slate-50 hover:bg-purple-50 rounded-xl p-4 transition-all duration-300 border border-slate-100 hover:border-purple-200 dark:bg-slate-800/40 dark:border-slate-700/50 dark:hover:border-purple-600/50">
                                        <div className="flex items-center gap-2 text-slate-400 text-xs mb-2 dark:text-slate-500">
                                            <div className="w-6 h-6 rounded-lg bg-white flex items-center justify-center shadow-sm dark:bg-slate-700/50">
                                                <PhoneOutlined className="text-purple-500 text-xs dark:text-purple-300/70" />
                                            </div>
                                            {t('pages.myTenantDashboard.contact.phone')}
                                        </div>
                                        <Text copyable className="text-sm font-medium text-slate-700 dark:text-slate-300">
                                            {formatValue(profile.contactPhone, t)}
                                        </Text>
                                    </div>

                                    <div className="group bg-slate-50 hover:bg-purple-50 rounded-xl p-4 transition-all duration-300 border border-slate-100 hover:border-purple-200 sm:col-span-2 dark:bg-slate-800/40 dark:border-slate-700/50 dark:hover:border-purple-800/50">
                                        <div className="flex items-center gap-2 text-slate-400 text-xs mb-2 dark:text-slate-500">
                                            <div className="w-6 h-6 rounded-lg bg-white flex items-center justify-center shadow-sm dark:bg-slate-700/50">
                                                <HomeOutlined className="text-purple-500 text-xs dark:text-purple-300/70" />
                                            </div>
                                            {t('pages.myTenantDashboard.contact.address')}
                                        </div>
                                        <Text copyable className="text-sm font-medium text-slate-700 dark:text-slate-300">
                                            {formatValue(profile.address, t)}
                                        </Text>
                                    </div>
                                </div>
                            </div>

                            <Divider className="my-4" />

                            <div>
                                <div className="flex items-center gap-2 mb-4">
                                    <div className="w-1 h-5 bg-gradient-to-b from-amber-400 to-amber-600 rounded-full"></div>
                                    <span className="text-sm font-medium text-slate-700 dark:text-slate-300">{t('pages.myTenantDashboard.timeInfo')}</span>
                                </div>
                                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                    <div className="group bg-slate-50 hover:bg-amber-50 rounded-xl p-4 transition-all duration-300 border border-slate-100 hover:border-amber-200 dark:bg-slate-800/40 dark:border-slate-700/50 dark:hover:border-amber-800/50">
                                        <div className="flex items-center gap-2 text-slate-400 text-xs mb-2 dark:text-slate-500">
                                            <div className="w-6 h-6 rounded-lg bg-white flex items-center justify-center shadow-sm dark:bg-slate-700/50">
                                                <CalendarOutlined className="text-amber-500 text-xs dark:text-amber-300/70" />
                                            </div>
                                            {t('pages.myTenantDashboard.time.createdTime')}
                                        </div>
                                        <div className="text-sm font-medium text-slate-700 dark:text-slate-300">
                                            {profile.createdTime ? formatTimestamp(profile.createdTime) : '-'}
                                        </div>
                                    </div>

                                    <div className="group bg-slate-50 hover:bg-amber-50 rounded-xl p-4 transition-all duration-300 border border-slate-100 hover:border-amber-200 dark:bg-slate-800/40 dark:border-slate-700/50 dark:hover:border-amber-800/50">
                                        <div className="flex items-center gap-2 text-slate-400 text-xs mb-2 dark:text-slate-500">
                                            <div className="w-6 h-6 rounded-lg bg-white flex items-center justify-center shadow-sm dark:bg-slate-700/50">
                                                <ClockCircleOutlined className="text-amber-500 text-xs dark:text-amber-300/70" />
                                            </div>
                                            {t('pages.myTenantDashboard.time.updatedTime')}
                                        </div>
                                        <div className="text-sm font-medium text-slate-700 dark:text-slate-300">
                                            {profile.modifiedTime ? formatTimestamp(profile.modifiedTime) : '-'}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </Card>
                    </Col>
                </Row>
            )}

            <ActionBarComponent title={t('pages.myTenantDashboard.joinedTenants.title')} subtitle={t('pages.myTenantDashboard.joinedTenants.subtitle')} />

            <Row gutter={[16, 16]}>
                {joinedTenants
                    .slice()
                    .sort((a, b) => {
                        const aIsCurrent = a.tenantId === currentTenant?.tenantId;
                        const bIsCurrent = b.tenantId === currentTenant?.tenantId;
                        return Number(bIsCurrent) - Number(aIsCurrent);
                    })
                    .map((tenant) => {
                        const isCurrent = tenant.tenantId === currentTenant?.tenantId;

                        return (
                            <Col xs={24} sm={12} lg={8} key={tenant.tenantId}>
                                <Card
                                    size="small"
                                    className={`rounded-xl overflow-hidden transition-all duration-300 ${isCurrent ? 'shadow-md' : undefined}`}
                                    style={{ borderColor: isCurrent ? token.colorPrimary : undefined }}
                                    styles={{ body: { padding: 16 } }}
                                >
                                    <div className="flex items-center gap-4">
                                        <Avatar
                                            size={56}
                                            src={tenant.tenantAvatar}
                                            icon={<ShopOutlined />}
                                            className="border-2"
                                            style={{ borderColor: token.colorBorderSecondary }}
                                        />
                                        <div className="flex-1 min-w-0">
                                            <div className="flex items-center gap-2 mb-1">
                                                <Text strong className="text-base truncate">
                                                    {tenant.tenantName}
                                                </Text>
                                                {isCurrent && <Tag color="blue">{t('pages.myTenantDashboard.joinedTenants.current')}</Tag>}
                                                <Tag color={statusColors[tenant.memberStatus] || 'default'} className="text-sm px-2 py-0.5">
                                                    {getTenantMemberStatus(tenant.memberStatus)}
                                                </Tag>
                                            </div>
                                            <div className="text-xs truncate">
                                                <Text type="secondary" copyable className="!text-xs">
                                                    ID: {tenant.tenantId}
                                                </Text>
                                            </div>
                                        </div>
                                    </div>
                                </Card>
                            </Col>
                        );
                    })}
            </Row>

            <div className="h-16"></div>
        </>
    );
}
