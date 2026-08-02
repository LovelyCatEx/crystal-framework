import {Popover, Space, Spin, Tag} from "antd";
import type {EntityTableColumns} from "../table/entity-table.types.ts";
import type {UserBanRecord} from "@/types/user/user-ban-record.types.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {useUserProfile} from "@/compositions/use-user-profile.ts";
import {UserCard} from "../card/pop/UserCard.tsx";
import {AvatarResource} from "../resource/AvatarResource.tsx";
import {formatTimestamp} from "@/utils/datetime.utils.ts";
import {useTranslation} from "react-i18next";

/**
 * Async user display: resolves a userId into a nickname/username cell,
 * falling back to an ID tag when the profile cannot be loaded.
 */
export function UserDisplay({userId}: { userId: string | null }) {
    const {t} = useTranslation();
    const {userProfile: user, isUserProfileLoading: isLoading} = useUserProfile(userId);

    if (!userId) {
        return <span className="text-xs text-gray-400">—</span>;
    }

    if (isLoading) {
        return <Spin size="small"/>;
    }

    if (user) {
        return (
            <Popover content={<UserCard userId={userId}/>} placement="right" trigger="hover">
                <Space size={8} className="cursor-pointer">
                    <AvatarResource url={user.avatar}/>
                    <Space orientation="vertical" size={0}>
                        <span className="text-xs font-mono font-bold">{user.nickname}</span>
                        <span className="text-xs text-gray-400">@{user.username}</span>
                    </Space>
                </Space>
            </Popover>
        );
    }

    return (
        <Space orientation="vertical" size={0}>
            <Tag color="red" className="m-0">{t('components.columns.userBanRecord.unknownUser')}</Tag>
            <CopyableToolTip title={userId}>
                <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {userId}</Tag>
            </CopyableToolTip>
        </Space>
    );
}

export type BanStatus = 'active' | 'lifted' | 'expired';

/**
 * Derives the ban status purely on the frontend from liftedTime + banUntil.
 * lifted  → manually unbanned (liftedTime set)
 * expired → temporary ban whose banUntil is in the past
 * active  → still in effect (permanent, or banUntil in the future)
 */
export function computeBanStatus(record: UserBanRecord, now: number = Date.now()): BanStatus {
    if (record.liftedTime) {
        return 'lifted';
    }
    if (record.banUntil && Number.parseInt(record.banUntil) <= now) {
        return 'expired';
    }
    return 'active';
}

const BAN_STATUS_COLOR: Record<BanStatus, string> = {
    active: 'red',
    lifted: 'green',
    expired: 'default',
};

export function useUserBanRecordTableColumns(): EntityTableColumns<UserBanRecord> {
    const {t} = useTranslation();

    return [
        {
            title: t('components.columns.userBanRecord.user'),
            dataIndex: "userId",
            key: "userId",
            width: 200,
            render: (_: unknown, record: UserBanRecord) => (
                <UserDisplay userId={record.userId}/>
            )
        },
        {
            title: t('components.columns.userBanRecord.reason'),
            dataIndex: "reason",
            key: "reason",
            width: 220,
            render: (_: unknown, record: UserBanRecord) => (
                record.reason ? (
                    <CopyableToolTip title={record.reason}>
                        <span className="text-xs truncate max-w-[200px] block">{record.reason}</span>
                    </CopyableToolTip>
                ) : <span className="text-xs text-gray-400">—</span>
            )
        },
        {
            title: t('components.columns.userBanRecord.banUntil'),
            dataIndex: "banUntil",
            key: "banUntil",
            width: 180,
            render: (_: unknown, record: UserBanRecord) => {
                if (!record.banUntil) {
                    return <Tag color="volcano">{t('components.columns.userBanRecord.permanent')}</Tag>;
                }
                const expired = Number.parseInt(record.banUntil) <= Date.now();
                return (
                    <Space orientation="vertical" size={0}>
                        <span className="text-xs font-mono">{formatTimestamp(record.banUntil)}</span>
                        {expired && (
                            <Tag color="default" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">
                                {t('components.columns.userBanRecord.status.expired')}
                            </Tag>
                        )}
                    </Space>
                );
            }
        },
        {
            title: t('components.columns.userBanRecord.status.label'),
            dataIndex: "status",
            key: "status",
            width: 120,
            render: (_: unknown, record: UserBanRecord) => {
                const status = computeBanStatus(record);
                return (
                    <Space orientation="vertical" size={0}>
                        <Tag color={BAN_STATUS_COLOR[status]}>
                            {t(`components.columns.userBanRecord.status.${status}`)}
                        </Tag>
                        {status === 'lifted' && record.liftedTime && (
                            <span className="text-xs text-gray-400">{formatTimestamp(record.liftedTime)}</span>
                        )}
                    </Space>
                );
            }
        },
        {
            title: t('components.columns.userBanRecord.operator'),
            dataIndex: "operatorUserId",
            key: "operatorUserId",
            width: 200,
            render: (_: unknown, record: UserBanRecord) => (
                <UserDisplay userId={record.operatorUserId}/>
            )
        }
    ];
}
