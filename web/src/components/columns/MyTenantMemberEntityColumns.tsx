import React, {type JSX} from "react";
import {Popover, Space, Tag} from "antd";
import type {EntityTableColumns} from "../types/entity-table.types.ts";
import {getTenantMemberStatus} from "@/i18n/enum-helpers.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {AvatarResource} from "../AvatarResource.tsx";
import {UserCard} from "../card/pop/UserCard.tsx";
import type {TenantMemberVO} from "@/types/tenant-member.types.ts";
import {useTranslation} from "react-i18next";

export function useMyTenantMemberTableColumns(): EntityTableColumns<TenantMemberVO> {
    const { t } = useTranslation();

    return [
        {
            title: t('components.columns.myTenantMember.recordInfo'),
            dataIndex: "id",
            key: "id",
            render: function (_: unknown, row: TenantMemberVO): React.ReactNode | JSX.Element {
                return <Space orientation='vertical' size={0}>
                    <CopyableToolTip title={row.id}>
                        <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {row.id}</Tag>
                    </CopyableToolTip>
                    <CopyableToolTip title={row.tenantId}>
                        <Tag color="orange" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">{t('components.columns.myTenantMember.tenantId')}: {row.tenantId}</Tag>
                    </CopyableToolTip>
                </Space>
            }
        },
        {
            title: t('components.columns.myTenantMember.member'),
            dataIndex: "user",
            key: "user",
            render: function (_: unknown, row: TenantMemberVO): React.ReactNode | JSX.Element {
                const user = row.user;
                return (
                    <Popover content={<UserCard userId={user.id} />} placement="right" trigger="hover">
                        <Space orientation='horizontal' size={8} className="cursor-pointer">
                            <AvatarResource fileEntityId={user.avatar} />
                            <Space orientation='vertical' size={0}>
                                <span className="text-xs font-mono font-bold">{user.nickname}</span>
                                <span className="text-xs font-mono text-gray-400">@{user.username}</span>
                            </Space>
                        </Space>
                    </Popover>
                );
            }
        },
        {
            title: t('components.columns.myTenantMember.email'),
            dataIndex: "email",
            key: "email",
            render: function (_: unknown, row: TenantMemberVO): React.ReactNode | JSX.Element {
                return <Space orientation='vertical' size={0}>
                    <CopyableToolTip title={row.user.email}>
                        <span className="text-xs font-mono">{row.user.email}</span>
                    </CopyableToolTip>
                </Space>
            }
        },
        {
            title: t('components.columns.myTenantMember.status'),
            dataIndex: "status",
            key: "status",
            render: function (_: unknown, row: TenantMemberVO): React.ReactNode | JSX.Element {
                const statusColors: Record<number, string> = {
                    0: 'default',
                    1: 'red',
                    2: 'orange',
                    3: 'blue',
                    4: 'green'
                };
                return <Tag color={statusColors[row.status] || 'default'} className="text-xs font-mono">
                    {getTenantMemberStatus(row.status)}
                </Tag>
            }
        }
    ];
}
