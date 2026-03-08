import React, {type JSX} from "react";
import {Space, Tag} from "antd";
import type {EntityTableColumns} from "../types/entity-table.types.ts";
import type {UserRole} from "@/types/user-role.types.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";

export const USER_ROLE_MANAGER_TABLE_COLUMNS: EntityTableColumns<UserRole> = [
    {
        title: "角色",
        dataIndex: "id",
        key: "id",
        render: function (_: unknown, row: UserRole): React.ReactNode | JSX.Element {
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
        title: "描述",
        dataIndex: "description",
        key: "description",
        render: function (_: unknown, row: UserRole): React.ReactNode | JSX.Element {
            return <Space orientation='vertical' size={0}>
                <CopyableToolTip title={row.description}>
                    <span className="text-xs font-mono">{row.description}</span>
                </CopyableToolTip>
            </Space>
        }
    }
];
