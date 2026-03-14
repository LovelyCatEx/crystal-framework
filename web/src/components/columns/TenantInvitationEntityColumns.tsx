import React, {type JSX, useEffect, useState} from "react";
import {Popover, Space, Spin, Tag, Tooltip, Typography} from "antd";
import type {EntityTableColumns} from "../types/entity-table.types.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import type {Invitation} from "@/types/tenant-invitation.types.ts";
import {CheckCircleOutlined, ClockCircleOutlined, CloseCircleOutlined} from "@ant-design/icons";
import {TenantMemberManagerController} from "@/api/tenant-member.api.ts";
import {TenantDepartmentManagerController} from "@/api/tenant-department.api.ts";
import type {TenantMemberVO} from "@/types/tenant-member.types.ts";
import type {TenantDepartment} from "@/types/tenant-department.types.ts";
import {UserCard} from "@/components/card/pop";
import {AvatarResource} from "@/components/AvatarResource.tsx";
const { Text } = Typography;

const MemberInfoDisplay: React.FC<{ tenantId: string, memberId: string }> = ({ tenantId, memberId }) => {
    const [member, setMember] = useState<TenantMemberVO | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        setLoading(true);
        TenantMemberManagerController.getById(memberId, { tenantId: tenantId })
            .then((data) => {
                setMember(data);
            })
            .catch(() => {
                setMember(null);
            })
            .finally(() => {
                setLoading(false);
            });
    }, [memberId]);

    if (loading) {
        return <Spin size="small" />;
    }

    if (!member) {
        return (
            <CopyableToolTip title={memberId}>
                <Tag color="red" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">
                    成员ID: {memberId}
                </Tag>
            </CopyableToolTip>
        );
    }

    const user = member.user;
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
                        <Tag color="purple" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">用户ID: {user.id}</Tag>
                    </CopyableToolTip>
                </Space>
            </Space>
        </Popover>
    );
};

const DepartmentInfoDisplay: React.FC<{ tenantId: string, departmentId: string }> = ({ tenantId, departmentId }) => {
    const [department, setDepartment] = useState<TenantDepartment | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        setLoading(true);
        TenantDepartmentManagerController.getById(departmentId, { tenantId: tenantId })
            .then((data) => {
                setDepartment(data);
            })
            .catch(() => {
                setDepartment(null);
            })
            .finally(() => {
                setLoading(false);
            });
    }, [departmentId]);

    if (loading) {
        return <Spin size="small" />;
    }

    if (!department) {
        return (
            <CopyableToolTip title={departmentId}>
                <Tag color="red" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">
                    部门ID: {departmentId}
                </Tag>
            </CopyableToolTip>
        );
    }

    return (
        <Space orientation="vertical" size={4}>
            <CopyableToolTip title={department.name}>
                <Tag color="cyan">
                    {department.name}
                </Tag>
            </CopyableToolTip>
            <CopyableToolTip title={departmentId}>
                <Tag color="blue">
                    ID: {department.id}
                </Tag>
            </CopyableToolTip>
        </Space>
    );
};

export const TENANT_INVITATION_TABLE_COLUMNS: EntityTableColumns<Invitation> = [
    {
        title: "记录信息",
        dataIndex: "id",
        key: "id",
        render: function (_: unknown, row: Invitation): React.ReactNode | JSX.Element {
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
        title: "邀请码",
        dataIndex: "invitationCode",
        key: "invitationCode",
        render: function (_: unknown, row: Invitation): React.ReactNode | JSX.Element {
            return (
                <Tag color="blue" className="px-3 py-1">
                    <Text copyable className="text-xs font-mono font-bold ">{row.invitationCode}</Text>
                </Tag>
            );
        }
    },
    {
        title: "可邀请次数",
        dataIndex: "invitationCount",
        key: "invitationCount",
        render: function (_: unknown, row: Invitation): React.ReactNode | JSX.Element {
            return (
                <Tag color={row.invitationCount > 0 ? "blue" : "red"} className="font-mono">
                    {row.invitationCount} 次
                </Tag>
            );
        }
    },
    {
        title: "创建者",
        dataIndex: "creatorMemberId",
        key: "creatorMemberId",
        render: function (_: unknown, row: Invitation): React.ReactNode | JSX.Element {
            return <MemberInfoDisplay tenantId={row.tenantId} memberId={row.creatorMemberId} />;
        }
    },
    {
        title: "部门",
        dataIndex: "departmentId",
        key: "departmentId",
        render: function (_: unknown, row: Invitation): React.ReactNode | JSX.Element {
            if (!row.departmentId) {
                return <Tag color="default" className="text-xs">未指定</Tag>;
            }
            return <DepartmentInfoDisplay tenantId={row.tenantId} departmentId={row.departmentId} />;
        }
    },
    {
        title: "需要审核",
        dataIndex: "requiresReviewing",
        key: "requiresReviewing",
        render: function (_: unknown, row: Invitation): React.ReactNode | JSX.Element {
            return row.requiresReviewing ? (
                <Tooltip title="需要审核">
                    <Tag color="orange" icon={<ClockCircleOutlined />}>
                        是
                    </Tag>
                </Tooltip>
            ) : (
                <Tooltip title="无需审核">
                    <Tag color="green" icon={<CheckCircleOutlined />}>
                        否
                    </Tag>
                </Tooltip>
            );
        }
    },
    {
        title: "过期时间",
        dataIndex: "expiresTime",
        key: "expiresTime",
        render: function (_: unknown, row: Invitation): React.ReactNode | JSX.Element {
            if (!row.expiresTime) {
                return <Tag color="default" className="text-xs">永不过期</Tag>;
            }
            const expiresDate = new Date(Number(row.expiresTime));
            const isExpired = expiresDate.getTime() < Date.now();
            return (
                <Tooltip title={expiresDate.toLocaleString()}>
                    <Tag color={isExpired ? "red" : "blue"} icon={isExpired ? <CloseCircleOutlined /> : <ClockCircleOutlined />} className="text-xs">
                        {isExpired ? "已过期" : expiresDate.toLocaleDateString()}
                    </Tag>
                </Tooltip>
            );
        }
    }
];
