import {Card, Descriptions, Spin, Tag} from "antd";
import type {User} from "@/types/user.types.ts";
import {useSWRComposition} from "@/compositions/swr.ts";
import {UserManagerController} from "@/api/user.api.ts";
import {CopyableToolTip} from "../../CopyableToolTip.tsx";
import {AvatarResource} from "../../AvatarResource.tsx";

interface UserCardProps {
    userId: string;
}

export function UserCard({ userId }: UserCardProps) {
    const { data: user, isLoading } = useSWRComposition<User | null>(
        `user-card-${userId}`,
        async () => {
            return await UserManagerController.getById(userId);
        }
    );

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
                    未找到用户信息
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
                    <AvatarResource fileEntityId={user.avatar} />
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
                <Descriptions.Item label="邮箱">
                    <CopyableToolTip title={user.email}>
                        <span className="text-gray-600">{user.email}</span>
                    </CopyableToolTip>
                </Descriptions.Item>
            </Descriptions>
        </Card>
    );
}
