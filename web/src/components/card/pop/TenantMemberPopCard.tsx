import {Avatar, Card, Descriptions, Spin, Tag} from "antd";
import {CopyableToolTip} from "../../CopyableToolTip.tsx";
import {UserOutlined} from "@ant-design/icons";
import {useTranslation} from "react-i18next";
import {useTenantMemberProfile} from "@/compositions/use-tenant-member-profile.ts";

interface TenantMemberPopCardProps {
    memberId: string;
}

export function TenantMemberPopCard({ memberId }: TenantMemberPopCardProps) {
    const { t } = useTranslation();
    const { member, isLoading } = useTenantMemberProfile(memberId);

    if (isLoading) {
        return (
            <Card size="small" className="w-64">
                <div className="flex justify-center py-4">
                    <Spin size="small" />
                </div>
            </Card>
        );
    }

    if (!member) {
        return (
            <Card size="small" className="w-64">
                <div className="text-center py-4 text-gray-400">
                    {t('components.popCard.tenantMember.notFound')}
                </div>
            </Card>
        );
    }

    return (
        <Card
            size="small"
            className="w-72"
            title={
                <div className="flex items-center gap-3 pt-2 pb-2">
                    <Avatar icon={<UserOutlined />} />
                    <div className="flex flex-col">
                        <CopyableToolTip title={member.nickname || member.name}>
                            <span className="font-bold">{member.nickname || member.name}</span>
                        </CopyableToolTip>
                    </div>
                </div>
            }
        >
            <Descriptions column={1} size="small" className="text-xs">
                <Descriptions.Item label={t('components.popCard.tenantMember.memberId')}>
                    <CopyableToolTip title={member.tenantMemberId}>
                        <Tag color="blue" className="text-xs">{member.tenantMemberId}</Tag>
                    </CopyableToolTip>
                </Descriptions.Item>
                {member.email && (
                    <Descriptions.Item label={t('components.popCard.tenantMember.email')}>
                        <CopyableToolTip title={member.email}>
                            <span className="text-gray-600">{member.email}</span>
                        </CopyableToolTip>
                    </Descriptions.Item>
                )}
            </Descriptions>
        </Card>
    );
}
