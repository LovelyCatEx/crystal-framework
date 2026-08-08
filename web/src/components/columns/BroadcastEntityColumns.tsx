import React from "react";
import {Popover, Space, Spin, Tag} from "antd";
import type {EntityTableColumns} from "@/components/table/entity-table.types.ts";
import {type Broadcast, AudienceType} from "@/types/message/broadcast.types.ts";
import {CopyableToolTip} from "@/components/CopyableToolTip.tsx";
import {useSWRComposition} from "@/compositions/use-swr.ts";
import {UserManagerController} from "@/api/user/user.api.ts";
import type {User} from "@/types/user/user.types.ts";
import {TenantManagerController} from "@/api/tenant/tenant.api.ts";
import type {Tenant} from "@/types/tenant/tenant.types.ts";
import {UserCard} from "@/components/card/pop/UserCard.tsx";
import {formatTimestamp} from "@/utils/datetime.utils.ts";
import {getAudienceType, getBroadcastCategory, getPartyType, getScopeType} from "@/i18n/enum-helpers.ts";
import {useTranslation} from "react-i18next";

// Resolves acting-user id (creator) into a name; falls back to a copyable id tag.
function ActingUserDisplay({userId}: {userId: string | null}) {
    const {t} = useTranslation();
    const {data: user, isLoading} = useSWRComposition<User | null>(
        userId ? `broadcast-acting-user-${userId}` : undefined,
        async () => (userId ? await UserManagerController.getById(userId) : null),
    );

    if (!userId) {
        return <Tag color="default" className="m-0">{t('components.columns.broadcast.systemSender')}</Tag>;
    }
    if (isLoading) {
        return <Spin size="small" />;
    }
    if (user) {
        return (
            <Popover content={<UserCard userId={userId} />} placement="right" trigger="hover">
                <span className="cursor-pointer">{user.nickname ?? user.username}</span>
            </Popover>
        );
    }
    return <CopyableToolTip title={userId}>
        <Tag color="orange" className="m-0">{t('components.columns.broadcast.unknown')}</Tag>
    </CopyableToolTip>;
}

// Resolves a tenant-member audience ref (tenantId) into the tenant name.
function AudienceTenantDisplay({tenantId}: {tenantId: string}) {
    const {t} = useTranslation();
    const {data: tenant, isLoading} = useSWRComposition<Tenant | null>(
        `broadcast-audience-tenant-${tenantId}`,
        async () => await TenantManagerController.getById(tenantId),
    );

    if (isLoading) {
        return <Spin size="small" />;
    }
    if (tenant) {
        return <Space size={4}>
            <span>{tenant.name}</span>
            <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {tenantId}</Tag>
        </Space>;
    }
    return <CopyableToolTip title={tenantId}>
        <Tag color="orange" className="m-0">{t('components.columns.broadcast.unknown')}</Tag>
    </CopyableToolTip>;
}

function AudienceDisplay({row}: {row: Broadcast}) {
    const {t} = useTranslation();
    const label = <Tag color="geekblue" className="m-0">{getAudienceType(row.audienceType)}</Tag>;

    switch (row.audienceType) {
        case AudienceType.ALL_USERS:
            return label;
        case AudienceType.TENANT_MEMBERS:
            return <Space size={6}>
                {label}
                {row.audienceRef ? <AudienceTenantDisplay tenantId={row.audienceRef} /> : null}
            </Space>;
        case AudienceType.SEGMENT:
            return <Space size={6}>
                {label}
                {row.audienceRef
                    ? <CopyableToolTip title={row.audienceRef}>
                        <Tag color="default" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">
                            {t('components.columns.broadcast.audienceRef')}: {row.audienceRef}
                        </Tag>
                    </CopyableToolTip>
                    : null}
            </Space>;
    }
}

export function useBroadcastTableColumns(): EntityTableColumns<Broadcast> {
    const {t} = useTranslation();

    return [
        {
            title: t('components.columns.broadcast.title'),
            dataIndex: 'title',
            key: 'title',
            width: 320,
            render: function (_: unknown, row: Broadcast): React.ReactNode {
                return <Space orientation="vertical" size={0}>
                    <span className="line-clamp-1">{row.title}</span>
                    <CopyableToolTip title={row.id}>
                        <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {row.id}</Tag>
                    </CopyableToolTip>
                </Space>;
            },
        },
        {
            title: t('components.columns.broadcast.content'),
            dataIndex: 'content',
            key: 'content',
            render: (val: unknown) => {
                const text = (val as string) ?? '';
                return <span className="line-clamp-2">{text.length > 128 ? `${text.slice(0, 128)}...` : text}</span>;
            },
        },
        {
            title: t('components.columns.broadcast.category'),
            dataIndex: 'category',
            key: 'category',
            width: 120,
            render: (val: unknown) => <Tag color="purple" className="m-0">{getBroadcastCategory(val as number)}</Tag>,
        },
        {
            title: t('components.columns.broadcast.scopeType'),
            dataIndex: 'scopeType',
            key: 'scopeType',
            width: 100,
            render: (val: unknown) => <Tag color="cyan" className="m-0">{getScopeType(val as number)}</Tag>,
        },
        {
            title: t('components.columns.broadcast.audience'),
            dataIndex: 'audienceType',
            key: 'audienceType',
            width: 220,
            render: (_: unknown, row: Broadcast) => <AudienceDisplay row={row} />,
        },
        {
            title: t('components.columns.broadcast.sender'),
            dataIndex: 'actingUserId',
            key: 'actingUserId',
            width: 180,
            render: (_: unknown, row: Broadcast) => <Space orientation="vertical" size={2}>
                <Tag color="gold" className="m-0 w-fit">{getPartyType(row.senderPartyType)}</Tag>
                <ActingUserDisplay userId={row.actingUserId} />
            </Space>,
        },
        {
            title: t('components.columns.broadcast.publishTime'),
            dataIndex: 'publishTime',
            key: 'publishTime',
            width: 170,
            render: (val: unknown) => <span>{formatTimestamp(val as string)}</span>,
        },
        {
            title: t('components.columns.broadcast.expireTime'),
            dataIndex: 'expireTime',
            key: 'expireTime',
            width: 170,
            render: (val: unknown) => <span>{val ? formatTimestamp(val as string) : '—'}</span>,
        },
    ];
}
