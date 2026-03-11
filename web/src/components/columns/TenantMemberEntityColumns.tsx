import React, {type JSX} from "react";
import {Popover, Space, Tag} from "antd";
import type {EntityTableColumns} from "../types/entity-table.types.ts";
import {TenantMemberStatusMap} from "@/api/tenant-member.api.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {UserAvatar} from "../UserAvatar.tsx";
import {formatTimestamp} from "@/utils/datetime.utils.ts";
import {UserCard} from "../card/pop/UserCard.tsx";
import type {TenantMemberVO} from "@/types/tenant-member.types.ts";

export const TENANT_MEMBER_TABLE_COLUMNS: EntityTableColumns<TenantMemberVO> = [
    {
        title: "成员信息",
        dataIndex: "id",
        key: "id",
        render: function (_: unknown, row: TenantMemberVO): React.ReactNode | JSX.Element {
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
        title: "用户信息",
        dataIndex: "user",
        key: "user",
        render: function (_: unknown, row: TenantMemberVO): React.ReactNode | JSX.Element {
            const user = row.user;
            return (
                <Popover content={<UserCard userId={user.id} />} placement="right" trigger="hover">
                    <Space orientation='horizontal' size={8} className="cursor-pointer">
                        <UserAvatar fileEntityId={user.avatar} />
                        <Space orientation='vertical' size={0}>
                            <CopyableToolTip title={user.username}>
                                <span className="text-xs font-mono font-bold">@{user.username}</span>
                            </CopyableToolTip>
                            <CopyableToolTip title={user.nickname}>
                                <span className="text-xs font-mono">{user.nickname}</span>
                            </CopyableToolTip>
                            <CopyableToolTip title={user.id}>
                                <Tag color="purple" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">用户ID: {user.id}</Tag>
                            </CopyableToolTip>
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
            return <Tag color={statusInfo.color} className="text-xs font-mono">
                {statusInfo.label}
            </Tag>
        }
    },
    {
        title: "租户ID",
        dataIndex: "tenantId",
        key: "tenantId",
        render: function (_: unknown, row: TenantMemberVO): React.ReactNode | JSX.Element {
            return <CopyableToolTip title={row.tenantId}>
                <Tag color="orange" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">{row.tenantId}</Tag>
            </CopyableToolTip>
        }
    }
];
