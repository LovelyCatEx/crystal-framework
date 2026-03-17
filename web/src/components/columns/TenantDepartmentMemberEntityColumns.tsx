import React, {type JSX} from "react";
import {Popover, Space, Tag} from "antd";
import type {EntityTableColumns} from "../types/entity-table.types.ts";
import {getDepartmentMemberRoleType, getTenantMemberStatus} from "@/i18n/enum-helpers.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {AvatarResource} from "../AvatarResource.tsx";
import {UserCard} from "../card/pop/UserCard.tsx";
import type {TenantDepartmentMemberVO} from "@/types/tenant-department-member.types.ts";
import {useTranslation} from "react-i18next";

export function useTenantDepartmentMemberTableColumns(): EntityTableColumns<TenantDepartmentMemberVO> {
    const { t } = useTranslation();

    return [
        {
            title: t('components.columns.tenantDepartmentMember.recordInfo'),
            dataIndex: "id",
            key: "id",
            width: 200,
            render: function (_: unknown, row: TenantDepartmentMemberVO): React.ReactNode | JSX.Element {
                return <Space orientation='vertical' size={0}>
                    <CopyableToolTip title={row.id}>
                        <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">{t('components.columns.tenantDepartmentMember.memberId')}: {row.member.id}</Tag>
                    </CopyableToolTip>
                    <CopyableToolTip title={row.member?.tenantId}>
                        <Tag color="orange" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">{t('components.columns.tenantDepartmentMember.tenantId')}: {row.member?.tenantId}</Tag>
                    </CopyableToolTip>
                </Space>
            }
        },
        {
            title: t('components.columns.tenantDepartmentMember.userInfo'),
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
                            <AvatarResource fileEntityId={user.avatar} />
                            <Space orientation='vertical' size={0}>
                                <CopyableToolTip title={user.username}>
                                    <span className="text-xs font-mono font-bold">@{user.username}</span>
                                </CopyableToolTip>
                                <CopyableToolTip title={user.nickname}>
                                    <span className="text-xs font-mono">{user.nickname}</span>
                                </CopyableToolTip>
                                <CopyableToolTip title={user.id}>
                                    <Tag color="purple" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">{t('components.columns.tenantDepartmentMember.userId')}: {user.id}</Tag>
                                </CopyableToolTip>
                            </Space>
                        </Space>
                    </Popover>
                );
            }
        },
        {
            title: t('components.columns.tenantDepartmentMember.email'),
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
            title: t('components.columns.tenantDepartmentMember.memberStatus'),
            dataIndex: "memberStatus",
            key: "memberStatus",
            width: 100,
            render: function (_: unknown, row: TenantDepartmentMemberVO): React.ReactNode | JSX.Element {
                const status = row.member?.status;
                const statusColors: Record<number, string> = {
                    0: 'default',
                    1: 'red',
                    2: 'orange',
                    3: 'blue',
                    4: 'green'
                };
                return <Tag color={statusColors[status] || 'default'} className="text-xs font-mono">
                    {getTenantMemberStatus(status)}
                </Tag>
            }
        },
        {
            title: t('components.columns.tenantDepartmentMember.departmentRole'),
            dataIndex: "roleType",
            key: "roleType",
            width: 120,
            render: function (_: unknown, row: TenantDepartmentMemberVO): React.ReactNode | JSX.Element {
                const roleType = row.roleType;
                const roleColors: Record<number, string> = {
                    0: 'default',
                    1: 'blue',
                    2: 'red'
                };
                return <Tag color={roleColors[roleType] || 'default'} className="text-xs font-mono">
                    {getDepartmentMemberRoleType(roleType)}
                </Tag>
            }
        }
    ];
}
