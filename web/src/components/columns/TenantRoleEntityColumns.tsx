import React, {type JSX} from "react";
import {Space, Tag} from "antd";
import type {EntityTableColumns} from "../types/entity-table.types.ts";
import type {TenantRoleVO} from "@/api/tenant-role.api.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {formatTimestamp} from "@/utils/datetime.utils.ts";

export const TENANT_ROLE_TABLE_COLUMNS: EntityTableColumns<TenantRoleVO> = [
    {
        title: "角色信息",
        dataIndex: "id",
        key: "id",
        render: function (_: unknown, row: TenantRoleVO): React.ReactNode | JSX.Element {
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
        title: "角色名称",
        dataIndex: "name",
        key: "name",
        render: function (_: unknown, row: TenantRoleVO): React.ReactNode | JSX.Element {
            return <CopyableToolTip title={row.name}>
                <span className="text-xs font-mono font-bold">{row.name}</span>
            </CopyableToolTip>
        }
    },
    {
        title: "描述",
        dataIndex: "description",
        key: "description",
        render: function (_: unknown, row: TenantRoleVO): React.ReactNode | JSX.Element {
            return <span className="text-xs font-mono text-gray-600">{row.description || '-'}</span>
        }
    },
    {
        title: "父角色ID",
        dataIndex: "parentId",
        key: "parentId",
        render: function (_: unknown, row: TenantRoleVO): React.ReactNode | JSX.Element {
            if (row.parentId) {
                return <CopyableToolTip title={row.parentId}>
                    <Tag color="orange" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">{row.parentId}</Tag>
                </CopyableToolTip>
            }
            return <span className="text-xs font-mono text-gray-400">-</span>
        }
    },
    {
        title: "租户ID",
        dataIndex: "tenantId",
        key: "tenantId",
        render: function (_: unknown, row: TenantRoleVO): React.ReactNode | JSX.Element {
            return <CopyableToolTip title={row.tenantId}>
                <Tag color="purple" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">{row.tenantId}</Tag>
            </CopyableToolTip>
        }
    }
];
