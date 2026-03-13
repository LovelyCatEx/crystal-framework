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
import {TenantStatusMap} from "@/types/tenant.types.ts";
import {formatTimestamp} from "@/utils/datetime.utils.ts";
import {useEffect, useState} from "react";
import {getUserProfile} from "@/api/user.api.ts";
import type {UserProfileVO} from "@/types/user.types.ts";

const { Title, Text } = Typography;
const { useToken } = theme;

function formatValue(value: string | null | undefined): string {
    return value ?? '-';
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
                <ActionBarComponent title="我的组织" subtitle="查看您当前的组织信息" />
                <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 256 }}>
                    <Spin size="large" />
                </div>
            </>
        );
    }

    if (!joinedTenants || joinedTenants.length === 0) {
        return (
            <>
                <ActionBarComponent title="我的组织" subtitle="查看您当前的组织信息" />
                <Empty
                    description="您还没有加入任何组织"
                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                />
            </>
        );
    }

    const statusInfo = profile ? TenantStatusMap[profile.status] : null;
    const expireStatus = profile ? getExpireStatus(profile.expiresTime) : null;

    return (
        <>
            <ActionBarComponent title={currentTenant?.tenantName || '我的组织'} subtitle="查看您当前的组织信息" />

            {profile && (
                <Row gutter={[24, 24]} style={{ marginBottom: 24 }}>
                    <Col xs={24} lg={8}>
                        <Card className="rounded-2xl shadow-sm border-none">
                            <div className="text-center mb-6">
                                <Avatar
                                    size={100}
                                    src={profile.icon}
                                    icon={<ShopOutlined />}
                                    className="border-4 border-gray-100 mb-4"
                                />
                                <Title level={4} className="!mb-2">{profile.name}</Title>
                                {statusInfo && (
                                    <Tag color={statusInfo.color} className="text-sm px-3 py-1">
                                        {statusInfo.label}
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
                                <div className="bg-blue-50 rounded-xl p-4">
                                    <CalendarOutlined className="text-blue-500 text-lg mb-2" />
                                    <div className="text-xs text-slate-400 mb-1">订阅时间</div>
                                    <div className="text-sm font-medium text-slate-700">
                                        {profile.subscribedTime ? formatTimestamp(Number(profile.subscribedTime), 'YYYY-MM-DD') : '-'}
                                    </div>
                                </div>
                                <Tooltip title={expireStatus && profile.expiresTime ? (expireStatus.isExpired ? '已过期' : `剩余 ${expireStatus.daysLeft} 天`) : ''}>
                                    <div className={`rounded-xl p-4 cursor-pointer ${expireStatus?.isExpired ? 'bg-red-50' : expireStatus?.isNearExpire ? 'bg-orange-50' : 'bg-green-50'}`}>
                                        <ClockCircleOutlined className={`text-lg mb-2 ${expireStatus?.isExpired ? 'text-red-500' : expireStatus?.isNearExpire ? 'text-orange-500' : 'text-green-500'}`} />
                                        <div className="text-xs text-slate-400 mb-1">过期时间</div>
                                        <div className={`text-sm font-medium ${expireStatus?.isExpired ? 'text-red-600' : expireStatus?.isNearExpire ? 'text-orange-600' : 'text-green-600'}`}>
                                            {profile.expiresTime ? formatTimestamp(Number(profile.expiresTime), 'YYYY-MM-DD') : '-'}
                                        </div>
                                    </div>
                                </Tooltip>
                            </div>

                            <div className="bg-gray-50 rounded-xl p-4">
                                <div className="flex items-center justify-center gap-2 mb-2">
                                    <span className="text-xs text-slate-400">组织所有者</span>
                                </div>
                                <div className="flex items-center justify-center gap-3">
                                    {isOwnerLoading ? (
                                        <Spin size="small" />
                                    ) : owner ? (
                                        <>
                                            <Avatar size={36} src={owner.avatar} icon={<UserOutlined />} />
                                            <span className="text-sm font-medium text-slate-700">
                                                {formatValue(owner.nickname)}
                                            </span>
                                        </>
                                    ) : (
                                        <>
                                            <Avatar size={36} icon={<UserOutlined />} />
                                            <span className="text-sm text-slate-500">{formatValue(profile.ownerUserId)}</span>
                                        </>
                                    )}
                                </div>
                            </div>
                        </Card>
                    </Col>

                    <Col xs={24} lg={16}>
                        <Card className="rounded-2xl shadow-sm border-none h-full">
                            <div className="mb-6">
                                <Title level={5} className="!mb-1">详细信息</Title>
                                <Text type="secondary">查看组织的详细资料信息</Text>
                            </div>

                            <div className="mb-6">
                                <div className="flex items-center gap-2 mb-4">
                                    <div className="w-1 h-5 bg-gradient-to-b from-blue-400 to-blue-600 rounded-full"></div>
                                    <span className="text-sm font-medium text-slate-700">基本信息</span>
                                </div>
                                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                    <div className="group bg-slate-50 hover:bg-blue-50 rounded-xl p-4 transition-all duration-300 border border-slate-100 hover:border-blue-200">
                                        <div className="flex items-center gap-2 text-slate-400 text-xs mb-2">
                                            <div className="w-6 h-6 rounded-lg bg-white flex items-center justify-center shadow-sm">
                                                <ShopOutlined className="text-blue-500 text-xs" />
                                            </div>
                                            组织ID
                                        </div>
                                        <Text copyable className="text-sm font-medium text-slate-700 font-mono">
                                            {formatValue(profile.tenantId)}
                                        </Text>
                                    </div>

                                    <div className="group bg-slate-50 hover:bg-green-50 rounded-xl p-4 transition-all duration-300 border border-slate-100 hover:border-green-200">
                                        <div className="flex items-center gap-2 text-slate-400 text-xs mb-2">
                                            <div className="w-6 h-6 rounded-lg bg-white flex items-center justify-center shadow-sm">
                                                <span className="w-2 h-2 rounded-full bg-green-500"></span>
                                            </div>
                                            状态
                                        </div>
                                        <div className="text-sm font-medium">
                                            {statusInfo ? (
                                                <Tag color={statusInfo.color} className="!text-sm !px-3 !py-0.5">
                                                    {statusInfo.label}
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
                                    <span className="text-sm font-medium text-slate-700">联系信息</span>
                                </div>
                                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                    <div className="group bg-slate-50 hover:bg-purple-50 rounded-xl p-4 transition-all duration-300 border border-slate-100 hover:border-purple-200">
                                        <div className="flex items-center gap-2 text-slate-400 text-xs mb-2">
                                            <div className="w-6 h-6 rounded-lg bg-white flex items-center justify-center shadow-sm">
                                                <UserOutlined className="text-purple-500 text-xs" />
                                            </div>
                                            联系人
                                        </div>
                                        <div className="text-sm font-medium text-slate-700">
                                            {formatValue(profile.contactName)}
                                        </div>
                                    </div>

                                    <div className="group bg-slate-50 hover:bg-purple-50 rounded-xl p-4 transition-all duration-300 border border-slate-100 hover:border-purple-200">
                                        <div className="flex items-center gap-2 text-slate-400 text-xs mb-2">
                                            <div className="w-6 h-6 rounded-lg bg-white flex items-center justify-center shadow-sm">
                                                <MailOutlined className="text-purple-500 text-xs" />
                                            </div>
                                            联系邮箱
                                        </div>
                                        <Text copyable className="text-sm font-medium text-slate-700">
                                            {formatValue(profile.contactEmail)}
                                        </Text>
                                    </div>

                                    <div className="group bg-slate-50 hover:bg-purple-50 rounded-xl p-4 transition-all duration-300 border border-slate-100 hover:border-purple-200">
                                        <div className="flex items-center gap-2 text-slate-400 text-xs mb-2">
                                            <div className="w-6 h-6 rounded-lg bg-white flex items-center justify-center shadow-sm">
                                                <PhoneOutlined className="text-purple-500 text-xs" />
                                            </div>
                                            联系电话
                                        </div>
                                        <Text copyable className="text-sm font-medium text-slate-700">
                                            {formatValue(profile.contactPhone)}
                                        </Text>
                                    </div>

                                    <div className="group bg-slate-50 hover:bg-purple-50 rounded-xl p-4 transition-all duration-300 border border-slate-100 hover:border-purple-200 sm:col-span-2">
                                        <div className="flex items-center gap-2 text-slate-400 text-xs mb-2">
                                            <div className="w-6 h-6 rounded-lg bg-white flex items-center justify-center shadow-sm">
                                                <HomeOutlined className="text-purple-500 text-xs" />
                                            </div>
                                            地址
                                        </div>
                                        <Text copyable className="text-sm font-medium text-slate-700">
                                            {formatValue(profile.address)}
                                        </Text>
                                    </div>
                                </div>
                            </div>

                            <Divider className="my-4" />

                            <div>
                                <div className="flex items-center gap-2 mb-4">
                                    <div className="w-1 h-5 bg-gradient-to-b from-amber-400 to-amber-600 rounded-full"></div>
                                    <span className="text-sm font-medium text-slate-700">时间信息</span>
                                </div>
                                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                    <div className="group bg-slate-50 hover:bg-amber-50 rounded-xl p-4 transition-all duration-300 border border-slate-100 hover:border-amber-200">
                                        <div className="flex items-center gap-2 text-slate-400 text-xs mb-2">
                                            <div className="w-6 h-6 rounded-lg bg-white flex items-center justify-center shadow-sm">
                                                <CalendarOutlined className="text-amber-500 text-xs" />
                                            </div>
                                            创建时间
                                        </div>
                                        <div className="text-sm font-medium text-slate-700">
                                            {profile.createdTime ? formatTimestamp(profile.createdTime) : '-'}
                                        </div>
                                    </div>

                                    <div className="group bg-slate-50 hover:bg-amber-50 rounded-xl p-4 transition-all duration-300 border border-slate-100 hover:border-amber-200">
                                        <div className="flex items-center gap-2 text-slate-400 text-xs mb-2">
                                            <div className="w-6 h-6 rounded-lg bg-white flex items-center justify-center shadow-sm">
                                                <ClockCircleOutlined className="text-amber-500 text-xs" />
                                            </div>
                                            修改时间
                                        </div>
                                        <div className="text-sm font-medium text-slate-700">
                                            {profile.modifiedTime ? formatTimestamp(profile.modifiedTime) : '-'}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </Card>
                    </Col>
                </Row>
            )}

            <ActionBarComponent title="已加入的组织" subtitle="查看您已加入的所有组织" />

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
                                    className={`rounded-xl overflow-hidden transition-all duration-300 ${isCurrent ? 'border-2 shadow-md' : 'border border-gray-200'}`}
                                    style={isCurrent ? { borderColor: token.colorPrimary } : undefined}
                                    styles={{ body: { padding: 16 } }}
                                >
                                    <div className="flex items-center gap-4">
                                        <Avatar
                                            size={56}
                                            src={tenant.tenantAvatar}
                                            icon={<ShopOutlined />}
                                            className="border-2 border-gray-100"
                                        />
                                        <div className="flex-1 min-w-0">
                                            <div className="flex items-center gap-2 mb-1">
                                                <Text strong className="text-base truncate">
                                                    {tenant.tenantName}
                                                </Text>
                                                {isCurrent && <Tag color="blue">当前</Tag>}
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
