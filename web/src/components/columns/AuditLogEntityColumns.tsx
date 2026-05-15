import React, {type JSX} from "react";
import {Popover, Space, Spin, Tag} from "antd";
import type {EntityTableColumns} from "../types/entity-table.types.ts";
import type {AuditLogEntity} from "@/api/audit-log.api.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {useSWRComposition} from "@/compositions/swr.ts";
import {UserManagerController} from "@/api/user.api.ts";
import type {User} from "@/types/user.types.ts";
import {UserCard} from "../card/pop/UserCard.tsx";
import {AvatarResource} from "../AvatarResource.tsx";
import {useTranslation} from "react-i18next";

function getAuditActionLabel(action: number, t: (key: string) => string): { label: string; color: string } {
    switch (action) {
        case 1: return { label: t('pages.auditLogManager.actionType.create'), color: 'green' };
        case 2: return { label: t('pages.auditLogManager.actionType.read'), color: 'blue' };
        case 3: return { label: t('pages.auditLogManager.actionType.update'), color: 'orange' };
        case 4: return { label: t('pages.auditLogManager.actionType.delete'), color: 'red' };
        default: return { label: t('pages.auditLogManager.actionType.unknown'), color: 'default' };
    }
}

function AuditUserCell({ userId, username }: { userId: string; username: string }) {
    const { data: user, isLoading } = useSWRComposition<User | null>(
        `audit-user-${userId}`,
        async () => {
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
                    <Space direction="vertical" size={0}>
                        <span className="text-xs font-mono font-bold">{user.nickname}</span>
                        <span className="text-xs text-gray-400">@{user.username}</span>
                    </Space>
                </Space>
            </Popover>
        );
    }

    return <Space direction='vertical' size={0}>
        <CopyableToolTip title={username}>
            <span className="text-xs font-mono">@{username}</span>
        </CopyableToolTip>
        <CopyableToolTip title={userId}>
            <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {userId}</Tag>
        </CopyableToolTip>
    </Space>;
}

export function useAuditLogTableColumns(): EntityTableColumns<AuditLogEntity> {
    const { t } = useTranslation();

    return [
        {
            title: t('components.columns.auditLog.userInfo'),
            dataIndex: "username",
            key: "username",
            width: 180,
            render: function (_: unknown, row: AuditLogEntity): React.ReactNode | JSX.Element {
                return <AuditUserCell userId={row.userId} username={row.username} />;
            }
        },
        {
            title: t('components.columns.auditLog.action'),
            dataIndex: "action",
            key: "action",
            width: 100,
            render: function (_: unknown, row: AuditLogEntity): React.ReactNode | JSX.Element {
                const { label, color } = getAuditActionLabel(row.action, t);
                return <Tag color={color}>{label}</Tag>;
            }
        },
        {
            title: t('components.columns.auditLog.resourceType'),
            dataIndex: "resourceType",
            key: "resourceType",
            width: 160,
            render: function (_: unknown, row: AuditLogEntity): React.ReactNode | JSX.Element {
                return <Space direction='vertical' size={0}>
                    <CopyableToolTip title={row.resourceType}>
                        <span className="text-xs font-mono">{row.resourceType}</span>
                    </CopyableToolTip>
                    {row.resourceIds && (
                        <CopyableToolTip title={row.resourceIds}>
                            <Tag color="purple" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">
                                IDs: {row.resourceIds}
                            </Tag>
                        </CopyableToolTip>
                    )}
                </Space>
            }
        },
        {
            title: t('components.columns.auditLog.request'),
            dataIndex: "path",
            key: "path",
            width: 240,
            render: function (_: unknown, row: AuditLogEntity): React.ReactNode | JSX.Element {
                return <Space direction='vertical' size={0}>
                    {row.httpMethod && row.path && (
                        <CopyableToolTip title={`${row.httpMethod} ${row.path}`}>
                            <span className="text-xs font-mono">
                                <Tag color="cyan" className="m-0 text-[10px] leading-4 h-4 px-1 rounded mr-1">
                                    {row.httpMethod}
                                </Tag>
                                {row.path}
                            </span>
                        </CopyableToolTip>
                    )}
                    {row.remoteIp && (
                        <CopyableToolTip title={row.remoteIp}>
                            <span className="text-xs text-gray-400">IP: {row.remoteIp}</span>
                        </CopyableToolTip>
                    )}
                </Space>
            }
        },
        {
            title: t('components.columns.auditLog.status'),
            dataIndex: "success",
            key: "success",
            width: 100,
            render: function (_: unknown, row: AuditLogEntity): React.ReactNode | JSX.Element {
                return <Space direction='vertical' size={0}>
                    <Tag color={row.success ? 'green' : 'red'}>
                        {row.success ? t('components.columns.auditLog.success') : t('components.columns.auditLog.failed')}
                    </Tag>
                    {row.errorMessage && (
                        <CopyableToolTip title={row.errorMessage}>
                            <span className="text-xs text-red-400 truncate max-w-[120px] block">{row.errorMessage}</span>
                        </CopyableToolTip>
                    )}
                </Space>
            }
        }
    ];
}
