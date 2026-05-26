import {Popover, Space, Spin, Tag} from "antd";
import type {EntityTableColumns} from "../table/entity-table.types.ts";
import type {MailSendLogEntity} from "@/types/mail/mail-send-log.types.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {useSWRComposition} from "@/compositions/use-swr.ts";
import {UserManagerController} from "@/api/user/user.api.ts";
import type {User} from "@/types/user/user.types.ts";
import {UserCard} from "../card/pop/UserCard.tsx";
import {AvatarResource} from "../AvatarResource.tsx";
import {useTranslation} from "react-i18next";

function MailUserCell({userId}: { userId: string | null }) {
    const {data: user, isLoading} = useSWRComposition<User | null>(
        userId ? `mail-user-${userId}` : undefined,
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

    return <span className="text-xs text-gray-400">—</span>;
}

export function useMailSendLogTableColumns(): EntityTableColumns<MailSendLogEntity> {
    const {t} = useTranslation();

    return [
        {
            title: t('components.columns.mailSendLog.fromEmail'),
            dataIndex: "fromEmail",
            key: "fromEmail",
            width: 180,
            render: (_: unknown, row: MailSendLogEntity) => (
                <CopyableToolTip title={row.fromEmail}>
                    <span className="text-xs font-mono">{row.fromEmail}</span>
                </CopyableToolTip>
            )
        },
        {
            title: t('components.columns.mailSendLog.toEmail'),
            dataIndex: "toEmail",
            key: "toEmail",
            width: 180,
            render: (_: unknown, row: MailSendLogEntity) => (
                <CopyableToolTip title={row.toEmail}>
                    <span className="text-xs font-mono">{row.toEmail}</span>
                </CopyableToolTip>
            )
        },
        {
            title: t('components.columns.mailSendLog.subject'),
            dataIndex: "subject",
            key: "subject",
            width: 200,
            render: (_: unknown, row: MailSendLogEntity) => (
                <CopyableToolTip title={row.subject}>
                    <span className="text-xs truncate max-w-[180px]">{row.subject}</span>
                </CopyableToolTip>
            )
        },
        {
            title: t('components.columns.mailSendLog.user'),
            dataIndex: "userId",
            key: "userId",
            width: 150,
            render: (_: unknown, record: MailSendLogEntity) => (
                <MailUserCell userId={record.userId} />
            )
        },
        {
            title: t('components.columns.mailSendLog.status'),
            dataIndex: "success",
            key: "success",
            width: 100,
            render: (_: unknown, record: MailSendLogEntity) => (
                <Space direction='vertical' size={0}>
                    <Tag color={record.success ? 'green' : 'red'}>
                        {record.success ? t('components.columns.mailSendLog.success') : t('components.columns.mailSendLog.failed')}
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
