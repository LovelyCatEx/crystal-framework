import React, {type JSX, useEffect, useState} from "react";
import {Space, Spin, Tag} from "antd";
import type {EntityTableColumns} from "../types/entity-table.types.ts";
import type {TenantDepartment} from "@/types/tenant-department.types.ts";
import {TenantDepartmentManagerController} from "@/api/tenant-department.api.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";

const ParentDepartmentDisplay: React.FC<{ tenantId: string, parentId: string }> = ({ tenantId, parentId }) => {
    const [department, setDepartment] = useState<TenantDepartment | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        setLoading(true);
        TenantDepartmentManagerController.getById(parentId, { tenantId: tenantId })
            .then((data) => {
                setDepartment(data);
            })
            .catch(() => {
                setDepartment(null);
            })
            .finally(() => {
                setLoading(false);
            });
    }, [parentId, tenantId]);

    if (loading) {
        return <Spin size="small" />;
    }

    if (!department) {
        return (
            <CopyableToolTip title={parentId}>
                <Tag color="red" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">
                    父部门ID: {parentId}
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
            <CopyableToolTip title={parentId}>
                <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">
                    ID: {department.id}
                </Tag>
            </CopyableToolTip>
        </Space>
    );
};

export const TENANT_DEPARTMENT_TABLE_COLUMNS: EntityTableColumns<TenantDepartment> = [
    {
        title: "部门名称",
        dataIndex: "name",
        key: "name",
        render: (value: unknown) => String(value),
    },
    {
        title: "描述",
        dataIndex: "description",
        key: "description",
        render: (value: unknown) => value ? String(value) : "-",
    },
    {
        title: "父部门",
        dataIndex: "parentId",
        key: "parentId",
        render: function (_: unknown, row: TenantDepartment): React.ReactNode | JSX.Element {
            if (!row.parentId) {
                return <Tag color="default" className="text-xs">-</Tag>;
            }
            return <ParentDepartmentDisplay tenantId={row.tenantId} parentId={row.parentId} />;
        }
    },
];
