import React, {type JSX} from "react";
import {Space, Spin, Tag} from "antd";
import type {EntityTableColumns} from "../types/entity-table.types.ts";
import type {OAuthAccount} from "../../types/oauth-account.types.ts";
import {OAuthPlatform} from "../../types/oauth-account.types.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {useSWRComposition} from "../../compositions/swr.ts";
import {UserManagerController} from "../../api/user.api.ts";
import type {User} from "../../types/user.types.ts";
import {UserAvatar} from "../UserAvatar.tsx";

function SystemUserCell({ userId }: { userId: string | null }) {
    const { data: user, isLoading } = useSWRComposition<User | null>(
        userId ? `user-${userId}` : undefined,
        async () => {
            if (!userId) return null;
            return await UserManagerController.getById(userId);
        }
    );

    if (!userId) {
        return <Tag color="default" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">未绑定用户</Tag>;
    }

    if (isLoading) {
        return <Spin size="small" />;
    }

    if (user) {
        return <Space orientation='horizontal' size={8}>
            <UserAvatar fileEntityId={user.avatar} />
            <Space orientation='vertical' size={0}>
                <CopyableToolTip title={user.nickname}>
                    <span>{user.nickname}</span>
                </CopyableToolTip>
                <CopyableToolTip title={user.username}>
                    <span className="text-gray-400">@{user.username}</span>
                </CopyableToolTip>
            </Space>
        </Space>;
    }

    return <CopyableToolTip title={userId}>
        <Tag color="green" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">
            用户ID: {userId}
        </Tag>
    </CopyableToolTip>;
}

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
            return <SystemUserCell userId={row.userId} />;
        }
    },
    {
        title: "用户信息",
        dataIndex: "nickname",
        key: "nickname",
        width: 240,
        render: function (_: unknown, row: OAuthAccount): React.ReactNode | JSX.Element {
            return <Space orientation='horizontal' size={8}>
                {row.avatar ? (
                    <img src={row.avatar} alt="avatar" className="w-8 h-8 rounded-full shrink-0 object-cover"  />
                ) : (
                    <Tag color="default" className="text-xs">无头像</Tag>
                )}
                <CopyableToolTip title={row.nickname ?? ''}>
                    <span className="text-xs font-mono">{row.nickname ?? '-'}</span>
                </CopyableToolTip>
            </Space>
        }
    }
];
