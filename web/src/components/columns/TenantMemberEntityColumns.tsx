import React, {type JSX} from "react";
import {Popover, Space, Tag} from "antd";
import type {EntityTableColumns} from "../types/entity-table.types.ts";
import {TenantMemberStatusMap} from "@/types/tenant-member.types.ts";
import {tenantMemberStatusToTranslationMap} from "@/i18n/tenant-member.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {AvatarResource} from "../AvatarResource.tsx";
import {UserCard} from "../card/pop/UserCard.tsx";
import type {TenantMemberVO} from "@/types/tenant-member.types.ts";

export const TENANT_MEMBER_TABLE_COLUMNS: EntityTableColumns<TenantMemberVO> = [
    {
        title: "记录信息",
        dataIndex: "id",
        key: "id",
        render: function (_: unknown, row: TenantMemberVO): React.ReactNode | JSX.Element {
            return <Space orientation='vertical' size={0}>
                <CopyableToolTip title={row.id}>
                    <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {row.id}</Tag>
                </CopyableToolTip>
                <CopyableToolTip title={row.tenantId}>
                    <Tag color="orange" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">租户 ID: {row.tenantId}</Tag>
                </CopyableToolTip>
            </Space>
        }
    },
    {
        title: "用户信息",
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
                            <Tag color="purple" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">用户ID: {user.id}</Tag>
                        </Space>
                    </Space>
                </Popover>
            );
        }
    },
    {
        title: "邮箱",
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
        title: "状态",
        dataIndex: "status",
        key: "status",
        render: function (_: unknown, row: TenantMemberVO): React.ReactNode | JSX.Element {
            const statusInfo = TenantMemberStatusMap[row.status] || { label: '未知', color: 'default' };
            const translatedLabel = tenantMemberStatusToTranslationMap.get(row.status) || statusInfo.label;
            return <Tag color={statusInfo.color} className="text-xs font-mono">
                {translatedLabel}
            </Tag>
        }
    }
];
