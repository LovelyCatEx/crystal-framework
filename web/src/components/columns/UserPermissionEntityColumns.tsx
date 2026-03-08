import React, {type JSX} from "react";
import {Space, Tag} from "antd";
import type {EntityTableColumns} from "../types/entity-table.types.ts";
import type {UserPermission} from "@/types/user-permission.types.ts";
import {PermissionType} from "@/types/user-permission.types.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";

export const USER_PERMISSION_MANAGER_TABLE_COLUMNS: EntityTableColumns<UserPermission> = [
    {
        title: "权限",
        dataIndex: "id",
        key: "id",
        render: function (_: unknown, row: UserPermission): React.ReactNode | JSX.Element {
            return <Space orientation='vertical' size={0}>
                <CopyableToolTip title={row.name}>
                    <span className="text-xs font-mono">{row.name}</span>
                </CopyableToolTip>
                <CopyableToolTip title={row.id}>
                    <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {row.id}</Tag>
                </CopyableToolTip>
            </Space>
        }
    },
    {
        title: "类型",
        dataIndex: "type",
        key: "type",
        render: function (_: unknown, row: UserPermission): React.ReactNode | JSX.Element {
            return <Space orientation='vertical' size={0}>
                <CopyableToolTip title={PermissionType[row.type]}>
                    <Tag color="orange" className="text-xs font-mono">{PermissionType[row.type]}</Tag>
                </CopyableToolTip>
            </Space>
        }
    },
    {
        title: "描述",
        dataIndex: "description",
        key: "description",
        render: function (_: unknown, row: UserPermission): React.ReactNode | JSX.Element {
            return <Space orientation='vertical' size={0}>
                <CopyableToolTip title={row.description}>
                    <span className="text-xs font-mono">{row.description}</span>
                </CopyableToolTip>
            </Space>
        }
    },
    {
        title: "资源路径",
        dataIndex: "path",
        key: "path",
        render: function (_: unknown, row: UserPermission): React.ReactNode | JSX.Element {
            return <Space orientation='vertical' size={0}>
                <CopyableToolTip title={row.path}>
                    <span className="text-xs font-mono">{row.path}</span>
                </CopyableToolTip>
            </Space>
        }
    }
];
