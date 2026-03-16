import React, {type JSX} from "react";
import {Space, Tag} from "antd";
import type {EntityTableColumns} from "../types/entity-table.types.ts";
import type {User} from "@/types/user.types.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {AvatarResource} from "../AvatarResource.tsx";

export const USER_MANAGER_TABLE_COLUMNS: EntityTableColumns<User> = [
    {
        title: "用户信息",
        dataIndex: "id",
        key: "id",
        render: function (_: unknown, row: User): React.ReactNode | JSX.Element {
            return <Space orientation='horizontal' size={8}>
                <AvatarResource fileEntityId={row.avatar} />

                <Space orientation='vertical' size={0}>
                    <CopyableToolTip title={row.username}>
                        <span className="text-xs font-mono">@{row.username}</span>
                    </CopyableToolTip>
                    <CopyableToolTip title={row.id}>
                        <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {row.id}</Tag>
                    </CopyableToolTip>
                </Space>
            </Space>
        }
    },
    {
        title: "昵称",
        dataIndex: "nickname",
        key: "nickname",
        render: function (_: unknown, row: User): React.ReactNode | JSX.Element {
            return <Space orientation='vertical' size={8}>
                <CopyableToolTip title={row.nickname}>
                    <span className="text-xs font-mono">{row.nickname}</span>
                </CopyableToolTip>
            </Space>
        }
    },
    {
        title: "邮箱",
        dataIndex: "email",
        key: "email",
        render: function (_: unknown, row: User): React.ReactNode | JSX.Element {
            return <Space orientation='vertical' size={0}>
                <CopyableToolTip title={row.email}>
                    <span className="text-xs font-mono">{row.email}</span>
                </CopyableToolTip>
            </Space>
        }
    }
];