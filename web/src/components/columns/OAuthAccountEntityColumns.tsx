import React, {type JSX} from "react";
import {Popover, Space, Spin, Tag} from "antd";
import type {EntityTableColumns} from "../types/entity-table.types.ts";
import type {OAuthAccount} from "@/types/oauth-account.types.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {useSWRComposition} from "@/compositions/swr.ts";
import {UserManagerController} from "@/api/user.api.ts";
import type {User} from "@/types/user.types.ts";
import {AvatarResource} from "../AvatarResource.tsx";
import PlatformIcon from "../PlatformIcon.tsx";
import {UserCard} from "../card/pop/UserCard.tsx";
import {useTranslation} from "react-i18next";

function SystemUserCell({ userId }: { userId: string | null }) {
    const { t } = useTranslation();
    const { data: user, isLoading } = useSWRComposition<User | null>(
        userId ? `user-${userId}` : undefined,
        async () => {
            if (!userId) return null;
            return await UserManagerController.getById(userId);
        }
    );

    if (!userId) {
        return <Tag color="default" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">{t('components.columns.oAuthAccount.unbound')}</Tag>;
    }

    if (isLoading) {
        return <Spin size="small" />;
    }

    if (user) {
        return (
            <Popover content={<UserCard userId={userId} />} placement="right" trigger="hover">
                <Space orientation='horizontal' size={8} className="cursor-pointer">
                    <AvatarResource fileEntityId={user.avatar} />
                    <Space orientation='vertical' size={0}>
                        <span>{user.nickname}</span>
                        <span className="text-gray-400">@{user.username}</span>
                    </Space>
                </Space>
            </Popover>
        );
    }

    return <CopyableToolTip title={userId}>
        <Tag color="green" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">
            {t('components.columns.oAuthAccount.userId')}: {userId}
        </Tag>
    </CopyableToolTip>;
}

export function useOAuthAccountTableColumns(): EntityTableColumns<OAuthAccount> {
    const { t } = useTranslation();

    return [
        {
            title: t('components.columns.oAuthAccount.identifier'),
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
            title: t('components.columns.oAuthAccount.platform'),
            dataIndex: "platform",
            key: "platform",
            render: function (_: unknown, row: OAuthAccount): React.ReactNode | JSX.Element {
                return <Space orientation='vertical' size={0}>
                    <PlatformIcon platform={row.platform} />
                </Space>
            }
        },
        {
            title: t('components.columns.oAuthAccount.systemUser'),
            dataIndex: "userId",
            key: "userId",
            render: function (_: unknown, row: OAuthAccount): React.ReactNode | JSX.Element {
                return <SystemUserCell userId={row.userId} />;
            }
        },
        {
            title: t('components.columns.oAuthAccount.userInfo'),
            dataIndex: "nickname",
            key: "nickname",
            width: 240,
            render: function (_: unknown, row: OAuthAccount): React.ReactNode | JSX.Element {
                return <Space orientation='horizontal' size={8}>
                    {row.avatar ? (
                        <img src={row.avatar} alt="avatar" className="w-8 h-8 rounded-full shrink-0 object-cover"  />
                    ) : (
                        <Tag color="default" className="text-xs">{t('components.columns.oAuthAccount.noAvatar')}</Tag>
                    )}
                    <CopyableToolTip title={row.nickname ?? ''}>
                        <span className="text-xs font-mono">{row.nickname ?? '-'}</span>
                    </CopyableToolTip>
                </Space>
            }
        }
    ];
}
