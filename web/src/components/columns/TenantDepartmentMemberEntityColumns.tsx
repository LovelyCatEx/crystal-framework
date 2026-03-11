import React, {type JSX} from "react";
import {Popover, Space, Tag} from "antd";
import type {EntityTableColumns} from "../types/entity-table.types.ts";
import {TenantMemberStatusMap} from "@/api/tenant-member.api.ts";
import {departmentMemberRoleTypeToTranslationMap} from "@/i18n/department-member.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {UserAvatar} from "../UserAvatar.tsx";
import {UserCard} from "../card/pop/UserCard.tsx";
import type {TenantDepartmentMemberVO} from "@/types/tenant-department-member.types.ts";
import {DepartmentMemberRoleTypeMap} from "@/api/tenant-department-member.api.ts";

export const TENANT_DEPARTMENT_MEMBER_TABLE_COLUMNS: EntityTableColumns<TenantDepartmentMemberVO> = [
    {
        title: "记录信息",
        dataIndex: "id",
        key: "id",
        width: 200,
        render: function (_: unknown, row: TenantDepartmentMemberVO): React.ReactNode | JSX.Element {
            return <Space orientation='vertical' size={0}>
                <CopyableToolTip title={row.id}>
                    <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">成员 ID: {row.member.id}</Tag>
                </CopyableToolTip>
                <CopyableToolTip title={row.member?.tenantId}>
                    <Tag color="orange" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">租户 ID: {row.member?.tenantId}</Tag>
                </CopyableToolTip>
            </Space>
        }
    },
    {
        title: "用户信息",
        dataIndex: "member",
        key: "member",
        width: 280,
        render: function (_: unknown, row: TenantDepartmentMemberVO): React.ReactNode | JSX.Element {
            const member = row.member;
            const user = member?.user;
            if (!user) return <span>-</span>;
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
        width: 200,
        render: function (_: unknown, row: TenantDepartmentMemberVO): React.ReactNode | JSX.Element {
            const email = row.member?.user?.email;
            return <Space orientation='vertical' size={0}>
                <CopyableToolTip title={email}>
                    <span className="text-xs font-mono">{email || '-'}</span>
                </CopyableToolTip>
            </Space>
        }
    },
    {
        title: "成员状态",
        dataIndex: "memberStatus",
        key: "memberStatus",
        width: 100,
        render: function (_: unknown, row: TenantDepartmentMemberVO): React.ReactNode | JSX.Element {
            const status = row.member?.status;
            const statusInfo = TenantMemberStatusMap[status] || { label: '未知', color: 'default' };
            return <Tag color={statusInfo.color} className="text-xs font-mono">
                {statusInfo.label}
            </Tag>
        }
    },
    {
        title: "部门角色",
        dataIndex: "roleType",
        key: "roleType",
        width: 120,
        render: function (_: unknown, row: TenantDepartmentMemberVO): React.ReactNode | JSX.Element {
            const roleType = row.roleType;
            const roleInfo = DepartmentMemberRoleTypeMap[roleType] || { label: '未知', color: 'default' };
            const translatedLabel = departmentMemberRoleTypeToTranslationMap.get(roleType) || roleInfo.label;
            return <Tag color={roleInfo.color} className="text-xs font-mono">
                {translatedLabel}
            </Tag>
        }
    }
];
