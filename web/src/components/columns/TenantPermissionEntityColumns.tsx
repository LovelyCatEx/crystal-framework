import React, {type JSX} from "react";
import {Space, Tag} from "antd";
import type {EntityTableColumns} from "../types/entity-table.types.ts";
import {getTenantPermissionType} from "@/i18n/enum-helpers.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {formatTimestamp} from "@/utils/datetime.utils.ts";
import type {TenantPermission} from "@/types/tenant-permission.types.ts";
import {useTranslation} from "react-i18next";

export function useTenantPermissionTableColumns(): EntityTableColumns<TenantPermission> {
    const { t } = useTranslation();

    return [
        {
            title: t('components.columns.tenantPermission.permissionInfo'),
            dataIndex: "id",
            key: "id",
            render: function (_: unknown, row: TenantPermission): React.ReactNode | JSX.Element {
                return <Space orientation='vertical' size={0}>
                    <CopyableToolTip title={row.id}>
                        <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {row.id}</Tag>
                    </CopyableToolTip>
                    <span className="text-xs font-mono text-gray-500">
                        {t('components.columns.tenantPermission.createdTime')}: {formatTimestamp(row.createdTime)}
                    </span>
                </Space>
            }
        },
        {
            title: t('components.columns.tenantPermission.permissionName'),
            dataIndex: "name",
            key: "name",
            render: function (_: unknown, row: TenantPermission): React.ReactNode | JSX.Element {
                return <CopyableToolTip title={row.name}>
                    <span className="text-xs font-mono font-bold">{row.name}</span>
                </CopyableToolTip>
            }
        },
        {
            title: t('components.columns.tenantPermission.description'),
            dataIndex: "description",
            key: "description",
            render: function (_: unknown, row: TenantPermission): React.ReactNode | JSX.Element {
                return <span className="text-xs font-mono text-gray-600">{row.description || '-'}</span>
            }
        },
        {
            title: t('components.columns.tenantPermission.type'),
            dataIndex: "type",
            key: "type",
            render: function (_: unknown, row: TenantPermission): React.ReactNode | JSX.Element {
                const typeColors: Record<number, string> = {
                    0: 'blue',
                    1: 'green'
                };
                return <Tag color={typeColors[row.type] || 'default'} className="text-xs font-mono">
                    {getTenantPermissionType(row.type)}
                </Tag>
            }
        },
        {
            title: t('components.columns.tenantPermission.path'),
            dataIndex: "path",
            key: "path",
            render: function (_: unknown, row: TenantPermission): React.ReactNode | JSX.Element {
                return <CopyableToolTip title={row.path || '-'}>
                    <span className="text-xs font-mono text-gray-600">{row.path || '-'}</span>
                </CopyableToolTip>
            }
        }
    ];
}
