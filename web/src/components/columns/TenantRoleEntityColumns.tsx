import React, {type JSX, useEffect, useState} from "react";
import {Space, Spin, Tag} from "antd";
import type {EntityTableColumns} from "../types/entity-table.types.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import type {TenantRole} from "@/types/tenat-role.types.ts";
import {TenantRoleManagerController} from "@/api/tenant-role.api.ts";

function ParentRoleNameTag({ tenantId, parentId }: { parentId: string, tenantId: string }) {
    const [parentRole, setParentRole] = useState<TenantRole | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        let isMounted = true;
        setLoading(true);
        TenantRoleManagerController.getById(parentId, { tenantId: tenantId })
            .then((role) => {
                if (isMounted) {
                    setParentRole(role);
                }
            })
            .catch(() => {
                if (isMounted) {
                    setParentRole(null);
                }
            })
            .finally(() => {
                if (isMounted) {
                    setLoading(false);
                }
            });
        return () => {
            isMounted = false;
        };
    }, [parentId]);

    if (loading) {
        return <Spin size="small" />;
    }

    if (parentRole) {
        return (
            <CopyableToolTip title={parentRole.name}>
                <Tag color="orange" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">
                    {parentRole.name}
                </Tag>
            </CopyableToolTip>
        );
    }

    return (
        <CopyableToolTip title={parentId}>
            <Tag color="orange" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">{parentId}</Tag>
        </CopyableToolTip>
    );
}

export const TENANT_ROLE_TABLE_COLUMNS: EntityTableColumns<TenantRole> = [
    {
        title: "角色信息",
        dataIndex: "id",
        key: "id",
        render: function (_: unknown, row: TenantRole): React.ReactNode | JSX.Element {
            return <Space orientation='vertical' size={0}>
                <CopyableToolTip title={row.name}>
                    <span className="text-xs font-mono font-bold">{row.name}</span>
                </CopyableToolTip>
                <CopyableToolTip title={row.id}>
                    <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {row.id}</Tag>
                </CopyableToolTip>
            </Space>
        }
    },
    {
        title: "描述",
        dataIndex: "description",
        key: "description",
        render: function (_: unknown, row: TenantRole): React.ReactNode | JSX.Element {
            return <span className="text-xs font-mono text-gray-600">{row.description || '-'}</span>
        }
    },
    {
        title: "父角色",
        dataIndex: "parentId",
        key: "parentId",
        render: function (_: unknown, row: TenantRole): React.ReactNode | JSX.Element {
            if (row.parentId) {
                return <ParentRoleNameTag tenantId={row.tenantId} parentId={row.parentId} />;
            }
            return <span className="text-xs font-mono text-gray-400">-</span>;
        }
    }
];
