import {Popover, Space, Spin, Tag} from "antd";
import type {EntityTableColumns} from "../table/entity-table.types.ts";
import type {UserLoginLogEntity} from "@/types/user/user-login-log.types.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {useSWRComposition} from "@/compositions/use-swr.ts";
import {UserManagerController} from "@/api/user/user.api.ts";
import type {User} from "@/types/user/user.types.ts";
import {UserCard} from "../card/pop/UserCard.tsx";
import {AvatarResource} from "../AvatarResource.tsx";
import {useTranslation} from "react-i18next";

function getLoginMethodLabel(loginMethod: number, t: (key: string) => string): { label: string; color: string } {
    switch (loginMethod) {
        case 0: return { label: t('components.columns.userLoginLog.loginMethodTypes.password'), color: 'blue' };
        case 1: return { label: t('components.columns.userLoginLog.loginMethodTypes.oauth2'), color: 'green' };
        default: return { label: t('components.columns.userLoginLog.loginMethodTypes.unknown'), color: 'default' };
    }
}

function LoginUserCell({ userId, username }: { userId: string | null; username: string | null }) {
    const { data: user, isLoading } = useSWRComposition<User | null>(
        userId ? `login-user-${userId}` : undefined,
        async () => {
            if (!userId) return null;
            return await UserManagerController.getById(userId);
        }
    );

    if (isLoading) {
        return <Spin size="small" />;
    }

    if (user) {
        return (
            <Popover content={<UserCard userId={userId} />} placement="right" trigger="hover">
                <Space size={8} className="cursor-pointer">
                    <AvatarResource fileEntityId={user.avatar} />
                    <Space orientation="vertical" size={0}>
                        <span className="text-xs font-mono font-bold">{user.nickname}</span>
                        <span className="text-xs text-gray-400">@{user.username}</span>
                    </Space>
                </Space>
            </Popover>
        );
    }

    if (username) {
        return <Space direction='vertical' size={0}>
            <CopyableToolTip title={username}>
                <span className="text-xs font-mono">@{username}</span>
            </CopyableToolTip>
            {userId && (
                <CopyableToolTip title={userId}>
                    <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {userId}</Tag>
                </CopyableToolTip>
            )}
        </Space>;
    }

    return <span className="text-xs text-gray-400">—</span>;
}

export function useUserLoginLogTableColumns(): EntityTableColumns<UserLoginLogEntity> {
    const { t } = useTranslation();

    return [
        {
            title: t('components.columns.userLoginLog.user'),
            dataIndex: "userId",
            key: "userId",
            width: 180,
            render: (_: unknown, record: UserLoginLogEntity) => (
                <LoginUserCell userId={record.userId} username={record.username} />
            )
        },
        {
            title: t('components.columns.userLoginLog.loginMethod'),
            dataIndex: "loginMethod",
            key: "loginMethod",
            width: 120,
            render: (_: unknown, record: UserLoginLogEntity) => {
                const { label, color } = getLoginMethodLabel(record.loginMethod, t);
                return <Tag color={color}>{label}</Tag>;
            }
        },
        {
            title: t('components.columns.userLoginLog.oauth2Username'),
            dataIndex: "oauth2Username",
            key: "oauth2Username",
            width: 150,
            render: (_: unknown, record: UserLoginLogEntity) => (
                record.oauth2Username ? (
                    <CopyableToolTip title={record.oauth2Username}>
                        <span className="text-xs font-mono truncate max-w-[130px] block">{record.oauth2Username}</span>
                    </CopyableToolTip>
                ) : <span className="text-xs text-gray-400">—</span>
            )
        },
        {
            title: t('components.columns.userLoginLog.remoteIp'),
            dataIndex: "remoteIp",
            key: "remoteIp",
            width: 130,
            render: (_: unknown, record: UserLoginLogEntity) => (
                record.remoteIp ? (
                    <CopyableToolTip title={record.remoteIp}>
                        <span className="text-xs font-mono">{record.remoteIp}</span>
                    </CopyableToolTip>
                ) : <span className="text-xs text-gray-400">—</span>
            )
        },
        {
            title: t('components.columns.userLoginLog.userAgent'),
            dataIndex: "userAgent",
            key: "userAgent",
            width: 200,
            render: (_: unknown, record: UserLoginLogEntity) => (
                record.userAgent ? (
                    <CopyableToolTip title={record.userAgent}>
                        <span className="text-xs truncate max-w-[180px] block">{record.userAgent}</span>
                    </CopyableToolTip>
                ) : <span className="text-xs text-gray-400">—</span>
            )
        },
        {
            title: t('components.columns.userLoginLog.status'),
            dataIndex: "success",
            key: "success",
            width: 100,
            render: (_: unknown, record: UserLoginLogEntity) => (
                <Space direction='vertical' size={0}>
                    <Tag color={record.success ? 'green' : 'red'}>
                        {record.success ? t('components.columns.userLoginLog.success') : t('components.columns.userLoginLog.failed')}
                    </Tag>
                    {record.errorMessage && (
                        <CopyableToolTip title={record.errorMessage}>
                            <span className="text-xs text-red-400 truncate max-w-[100px] block">{record.errorMessage}</span>
                        </CopyableToolTip>
                    )}
                </Space>
            )
        }
    ];
}