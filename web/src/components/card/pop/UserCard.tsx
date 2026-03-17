import {Avatar, Card, Descriptions, Spin, Tag} from "antd";
import {CopyableToolTip} from "../../CopyableToolTip.tsx";
import {useUserProfile} from "@/compositions/use-user-profile.ts";
import {UserOutlined} from "@ant-design/icons";
import {useTranslation} from "react-i18next";

interface UserCardProps {
    userId?: string;
}

export function UserCard({ userId }: UserCardProps) {
    const { t } = useTranslation();
    const { userProfile: user, isUserProfileLoading: isLoading } = useUserProfile(userId);

    if (isLoading) {
        return (
            <Card size="small" className="w-64">
                <div className="flex justify-center py-4">
                    <Spin size="small" />
                </div>
            </Card>
        );
    }

    if (!user) {
        return (
            <Card size="small" className="w-64">
                <div className="text-center py-4 text-gray-400">
                    {t('components.popCard.user.notFound')}
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
                    <Avatar
                        src={user.avatar}
                        icon={<UserOutlined />}
                    />
                    <div className="flex flex-col">
                        <CopyableToolTip title={user.nickname}>
                            <span className="font-bold">{user.nickname}</span>
                        </CopyableToolTip>
                        <CopyableToolTip title={user.username}>
                            <span className="text-xs text-gray-400">@{user.username}</span>
                        </CopyableToolTip>
                    </div>
                </div>
            }
        >
            <Descriptions column={1} size="small" className="text-xs">
                <Descriptions.Item label="ID">
                    <CopyableToolTip title={user.id}>
                        <Tag color="blue" className="text-xs">{user.id}</Tag>
                    </CopyableToolTip>
                </Descriptions.Item>
                <Descriptions.Item label={t('components.popCard.user.email')}>
                    <CopyableToolTip title={user.email}>
                        <span className="text-gray-600">{user.email}</span>
                    </CopyableToolTip>
                </Descriptions.Item>
            </Descriptions>
        </Card>
    );
}
