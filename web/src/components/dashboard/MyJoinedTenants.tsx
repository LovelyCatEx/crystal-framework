import {Avatar, Button, Card, Col, Empty, Row, Spin, Tag, theme} from "antd";
import {useUserTenants} from "@/compositions/use-tenant.ts";
import {PlusOutlined, RightOutlined, ShopOutlined, TeamOutlined,} from "@ant-design/icons";
import {TenantMemberStatusMap} from "@/types/tenant-member.types.ts";
import {tenantMemberStatusToTranslationMap} from "@/i18n/tenant-member.ts";
import {useNavigate} from "react-router-dom";
import {useEffect, useState} from "react";
import {getTenantProfile} from "@/api/tenant-profile.api.ts";
import type {TenantProfileVO, UserTenantVO} from "@/types/tenant.types.ts";

const { useToken } = theme;

interface TenantWithProfile extends UserTenantVO {
    profile?: TenantProfileVO;
}

function formatDescription(description: string | null | undefined, maxLength: number = 48): string {
    if (!description) return "";
    if (description.length <= maxLength) return description;
    return description.slice(0, maxLength) + "...";
}

export function MyJoinedTenants() {
    const { token } = useToken();
    const { joinedTenants, isJoinedTenantsLoading, currentTenant } = useUserTenants();
    const navigate = useNavigate();
    const [tenantsWithProfile, setTenantsWithProfile] = useState<TenantWithProfile[]>([]);
    const [isLoadingProfiles, setIsLoadingProfiles] = useState(false);

    useEffect(() => {
        if (joinedTenants && joinedTenants.length > 0) {
            setIsLoadingProfiles(true);
            Promise.all(
                joinedTenants.map(async (tenant) => {
                    try {
                        const res = await getTenantProfile(tenant.tenantId);
                        return { ...tenant, profile: res.data } as TenantWithProfile;
                    } catch {
                        return { ...tenant } as TenantWithProfile;
                    }
                })
            )
                .then((results) => {
                    setTenantsWithProfile(results);
                })
                .finally(() => {
                    setIsLoadingProfiles(false);
                });
        } else {
            setTenantsWithProfile([]);
        }
    }, [joinedTenants]);

    const handleJoinByCode = () => {
        navigate("/tenant/invitation");
    };

    if (isJoinedTenantsLoading || isLoadingProfiles) {
        return (
            <Card
                title={
                    <div className="flex items-center gap-2">
                        <TeamOutlined style={{ color: token.colorPrimary }} />
                        <span className="text-sm font-bold text-slate-800">我加入的组织</span>
                    </div>
                }
                className="rounded-3xl border-none shadow-sm"
            >
                <div className="flex justify-center items-center py-12">
                    <Spin size="small" />
                </div>
            </Card>
        );
    }

    const sortedTenants = tenantsWithProfile
        .slice()
        .sort((a, b) => {
            const aIsCurrent = a.tenantId === currentTenant?.tenantId;
            const bIsCurrent = b.tenantId === currentTenant?.tenantId;
            return Number(bIsCurrent) - Number(aIsCurrent);
        });

    return (
        <Card
                title={
                    <div className="flex items-center justify-between w-full">
                        <div className="flex items-center gap-2">
                            <TeamOutlined />
                            <span className="text-sm font-bold text-slate-800">我加入的组织</span>
                            <span className="text-xs text-slate-400">
                                ({joinedTenants?.length || 0})
                            </span>
                        </div>
                    </div>
                }
                className="rounded-3xl border-none shadow-sm"
            >
                {sortedTenants.length === 0 ? (
                    <Empty
                        description="暂无加入的组织"
                        image={Empty.PRESENTED_IMAGE_SIMPLE}
                        className="py-8"
                    >
                        <Button onClick={handleJoinByCode}>通过邀请码加入</Button>
                    </Empty>
                ) : (
                    <Row gutter={[16, 16]}>
                        {sortedTenants.map((tenant) => {
                            const isCurrent = tenant.tenantId === currentTenant?.tenantId;
                            const statusInfo = TenantMemberStatusMap[tenant.memberStatus];
                            const description = formatDescription(tenant.profile?.description);

                            return (
                                <Col xs={24} sm={12} lg={8} key={tenant.tenantId}>
                                    <div
                                        className="p-4 rounded-xl transition-all duration-300 h-full cursor-pointer border"
                                        style={isCurrent ? {
                                            borderColor: token.colorPrimary,
                                        } : {
                                            borderColor: token.colorBorder,
                                        }}
                                        onMouseEnter={(e) => {
                                            if (!isCurrent) {
                                                e.currentTarget.style.borderColor = token.colorPrimary;
                                            }
                                        }}
                                        onMouseLeave={(e) => {
                                            if (!isCurrent) {
                                                e.currentTarget.style.borderColor = token.colorBorder;
                                            }
                                        }}
                                    >
                                        <div className="flex items-start gap-3">
                                            <Avatar
                                                size={48}
                                                src={tenant.tenantAvatar}
                                                icon={<ShopOutlined />}
                                                style={{
                                                    flexShrink: 0
                                                }}
                                            />
                                            <div className="flex-1 min-w-0">
                                                <div className="flex items-center gap-2 flex-wrap">
                                                    <span className="text-sm font-medium truncate" style={{ color: token.colorTextHeading }}>
                                                        {tenant.tenantName}
                                                    </span>
                                                    {isCurrent && (
                                                        <Tag color="blue" className="!text-xs !px-1 !py-0">
                                                            当前
                                                        </Tag>
                                                    )}
                                                    <Tag
                                                        color={statusInfo?.color || "default"}
                                                        className="!text-xs !px-1 !py-0"
                                                    >
                                                        {tenantMemberStatusToTranslationMap.get(tenant.memberStatus) ||
                                                            statusInfo?.label ||
                                                            "未知"}
                                                    </Tag>
                                                </div>
                                                <div className="mt-2">
                                                    <span className="text-xs text-gray-400 line-clamp-2">{description || '暂无描述~'}</span>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </Col>
                            );
                        })}

                        <Col xs={24} sm={12} lg={8}>
                            <div
                                onClick={handleJoinByCode}
                                className="p-4 rounded-xl border-2 border-dashed h-full flex items-center gap-3 cursor-pointer transition-all duration-300"
                                style={{
                                    borderColor: token.colorBorderSecondary,
                                    backgroundColor: token.colorBgContainer
                                }}
                                onMouseEnter={(e) => {
                                    e.currentTarget.style.borderColor = token.colorPrimary;
                                }}
                                onMouseLeave={(e) => {
                                    e.currentTarget.style.borderColor = token.colorBorder;
                                }}
                            >
                                <div
                                    className="w-12 h-12 rounded-full flex items-center justify-center transition-colors flex-shrink-0"
                                    style={{ backgroundColor: token.colorFillTertiary }}
                                >
                                    <PlusOutlined
                                        className="text-lg transition-colors"
                                        style={{ color: token.colorTextSecondary }}
                                    />
                                </div>
                                <div className="flex-1">
                                    <span
                                        className="text-sm font-medium transition-colors"
                                        style={{ color: token.colorTextSecondary }}
                                    >
                                        通过邀请码加入
                                    </span>
                                    <div
                                        className="text-xs mt-1"
                                        style={{ color: token.colorTextDisabled }}
                                    >
                                        输入邀请码加入新组织
                                    </div>
                                </div>
                                <RightOutlined
                                    className="transition-colors"
                                    style={{ color: token.colorTextDisabled }}
                                />
                            </div>
                        </Col>
                    </Row>
                )}
            </Card>
    );
}
