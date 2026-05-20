import {CopyableToolTip} from "../CopyableToolTip.tsx";
import type {EntityTableColumns} from "../types/entity-table.types.ts";
import type {SessionDescription} from "@/types/session.types.ts";
import {useTranslation} from "react-i18next";
import {Popover, Space, Spin, Tag} from "antd";
import {useSWRComposition} from "@/compositions/swr.ts";
import {UserManagerController} from "@/api/user.api.ts";
import type {User} from "@/types/user.types.ts";
import {UserCard} from "../card/pop/UserCard.tsx";
import {AvatarResource} from "../AvatarResource.tsx";
import {TenantManagerController} from "@/api/tenant.api.ts";
import type {Tenant} from "@/types/tenant.types.ts";

function SessionUserCell({ userId }: { userId: number }) {
    const userIdStr = userId.toString();
    const { data: user, isLoading } = useSWRComposition<User | null>(
        `session-user-${userId}`,
        async () => {
            return await UserManagerController.getById(userIdStr);
        }
    );

    if (isLoading) {
        return <Spin size="small" />;
    }

    if (user) {
        return (
            <Popover content={<UserCard userId={userIdStr} />} placement="right" trigger="hover">
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

    return <Space orientation='vertical' size={0}>
        <CopyableToolTip title={userIdStr}>
            <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {userId}</Tag>
        </CopyableToolTip>
    </Space>;
}

function SessionTenantCell({ tenantId }: { tenantId: number }) {
    if (tenantId <= 0) {
        return <span className="text-gray-400">-</span>;
    }

    const tenantIdStr = tenantId.toString();
    const { data: tenant, isLoading } = useSWRComposition<Tenant | null>(
        `session-tenant-${tenantId}`,
        async () => {
            return await TenantManagerController.getById(tenantIdStr);
        }
    );

    if (isLoading) {
        return <Spin size="small" />;
    }

    if (tenant) {
        return <CopyableToolTip title={tenant.name}>
            <Tag color="green" className="text-xs font-mono">{tenant.name}</Tag>
        </CopyableToolTip>;
    }

    return <CopyableToolTip title={tenantIdStr}>
        <Tag color="purple" className="text-xs font-mono">{tenantId}</Tag>
    </CopyableToolTip>;
}

export function useSessionMonitorTableColumns(): EntityTableColumns<SessionDescription> {
    const {t} = useTranslation();

    return [
        {
            title: t('components.columns.sessionMonitor.sessionId'),
            dataIndex: "sessionId",
            key: "sessionId",
            width: 200,
            render: (_: unknown, record: SessionDescription) => (
                <CopyableToolTip title={record.sessionId}>
                    <Tag color="purple" className="text-xs font-mono">{record.sessionId.substring(0, 16)}...</Tag>
                </CopyableToolTip>
            )
        },
        {
            title: t('components.columns.sessionMonitor.user'),
            dataIndex: "userId",
            key: "userId",
            width: 150,
            render: (_: unknown, record: SessionDescription) => (
                <SessionUserCell userId={record.userId} />
            )
        },
        {
            title: t('components.columns.sessionMonitor.tenant'),
            dataIndex: "tenantId",
            key: "tenantId",
            width: 120,
            render: (_: unknown, record: SessionDescription) => (
                <SessionTenantCell tenantId={record.tenantId} />
            )
        },
        {
            title: t('components.columns.sessionMonitor.remoteIp'),
            dataIndex: "remoteIp",
            key: "remoteIp",
            width: 130,
            render: (_: unknown, record: SessionDescription) => (
                <CopyableToolTip title={record.remoteIp}>
                    <Tag color="blue" className="text-xs">{record.remoteIp}</Tag>
                </CopyableToolTip>
            )
        },
        {
            title: t('components.columns.sessionMonitor.userAgent'),
            dataIndex: "userAgent",
            key: "userAgent",
            width: 200,
            render: (_: unknown, record: SessionDescription) => (
                <CopyableToolTip title={record.userAgent}>
                    <span className="text-xs truncate max-w-[180px] block">{record.userAgent}</span>
                </CopyableToolTip>
            )
        }
    ];
}