import React, {type JSX} from "react";
import {Space, Tag} from "antd";
import type {EntityTableColumns} from "../types/entity-table.types.ts";
import type {OAuthAccount} from "../../types/oauth-account.types.ts";
import {OAuthPlatform} from "../../types/oauth-account.types.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";

export const OAUTH_ACCOUNT_MANAGER_TABLE_COLUMNS: EntityTableColumns<OAuthAccount> = [
    {
        title: "标识",
        dataIndex: "id",
        key: "id",
        render: function (_: unknown, row: OAuthAccount): React.ReactNode | JSX.Element {
            return <Space orientation='vertical' size={0}>
                <CopyableToolTip title={row.identifier}>
                    <span className="text-xs font-mono">{row.identifier}</span>
                </CopyableToolTip>
                <CopyableToolTip title={row.id}>
                    <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {row.id}</Tag>
                </CopyableToolTip>
            </Space>
        }
    },
    {
        title: "平台",
        dataIndex: "platform",
        key: "platform",
        render: function (_: unknown, row: OAuthAccount): React.ReactNode | JSX.Element {
            return <Space orientation='vertical' size={0}>
                <CopyableToolTip title={OAuthPlatform[row.platform]}>
                    <Tag color="orange" className="text-xs font-mono">{OAuthPlatform[row.platform]}</Tag>
                </CopyableToolTip>
            </Space>
        }
    },
    {
        title: "系统用户",
        dataIndex: "userId",
        key: "userId",
        render: function (_: unknown, row: OAuthAccount): React.ReactNode | JSX.Element {
            return <CopyableToolTip title={row.userId ?? '未绑定'}>
                <Tag color={row.userId ? "green" : "default"} className="m-0 text-[10px] leading-4 h-4 px-1 rounded">
                    {row.userId ? `用户ID: ${row.userId}` : '未绑定用户'}
                </Tag>
            </CopyableToolTip>
        }
    },
    {
        title: "用户信息",
        dataIndex: "nickname",
        key: "nickname",
        render: function (_: unknown, row: OAuthAccount): React.ReactNode | JSX.Element {
            return <Space orientation='horizontal' size={8}>
                {row.avatar ? (
                    <img src={row.avatar} alt="avatar" className="w-8 h-8 rounded-full" />
                ) : (
                    <Tag color="default" className="text-xs">无头像</Tag>
                )}
                <Space orientation='vertical' size={0}>
                    <CopyableToolTip title={row.nickname ?? ''}>
                        <span className="text-xs font-mono">昵称: {row.nickname ?? '-'}</span>
                    </CopyableToolTip>
                </Space>
            </Space>
        }
    }
];
