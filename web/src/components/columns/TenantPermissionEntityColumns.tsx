import React, {type JSX} from "react";
import {Space, Tag} from "antd";
import type {EntityTableColumns} from "../types/entity-table.types.ts";
import {TenantPermissionTypeMap} from "@/api/tenant-permission.api.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {formatTimestamp} from "@/utils/datetime.utils.ts";
import type {TenantPermission} from "@/types/tenant-permission.types.ts";

export const TENANT_PERMISSION_TABLE_COLUMNS: EntityTableColumns<TenantPermission> = [
    {
        title: "权限信息",
        dataIndex: "id",
        key: "id",
        render: function (_: unknown, row: TenantPermission): React.ReactNode | JSX.Element {
            return <Space orientation='vertical' size={0}>
                <CopyableToolTip title={row.id}>
                    <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {row.id}</Tag>
                </CopyableToolTip>
                <span className="text-xs font-mono text-gray-500">
                    创建时间: {formatTimestamp(row.createdTime)}
                </span>
            </Space>
        }
    },
    {
        title: "权限名称",
        dataIndex: "name",
        key: "name",
        render: function (_: unknown, row: TenantPermission): React.ReactNode | JSX.Element {
            return <CopyableToolTip title={row.name}>
                <span className="text-xs font-mono font-bold">{row.name}</span>
            </CopyableToolTip>
        }
    },
    {
        title: "描述",
        dataIndex: "description",
        key: "description",
        render: function (_: unknown, row: TenantPermission): React.ReactNode | JSX.Element {
            return <span className="text-xs font-mono text-gray-600">{row.description || '-'}</span>
        }
    },
    {
        title: "类型",
        dataIndex: "type",
        key: "type",
        render: function (_: unknown, row: TenantPermission): React.ReactNode | JSX.Element {
            const typeInfo = TenantPermissionTypeMap[row.type] || { label: '未知', color: 'default' };
            return <Tag color={typeInfo.color} className="text-xs font-mono">
                {typeInfo.label}
            </Tag>
        }
    },
    {
        title: "路径",
        dataIndex: "path",
        key: "path",
        render: function (_: unknown, row: TenantPermission): React.ReactNode | JSX.Element {
            return <CopyableToolTip title={row.path || '-'}>
                <span className="text-xs font-mono text-gray-600">{row.path || '-'}</span>
            </CopyableToolTip>
        }
    }
];
