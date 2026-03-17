import React, {type JSX, useEffect, useState} from "react";
import {Space, Spin, Tag} from "antd";
import type {EntityTableColumns} from "../types/entity-table.types.ts";
import type {TenantDepartment} from "@/types/tenant-department.types.ts";
import {TenantDepartmentManagerController} from "@/api/tenant-department.api.ts";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {useTranslation} from "react-i18next";

const ParentDepartmentDisplay: React.FC<{ tenantId: string, parentId: string }> = ({ tenantId, parentId }) => {
    const { t } = useTranslation();
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
                    {t('components.columns.tenantDepartment.parentDepartmentId')}: {parentId}
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

export function useTenantDepartmentTableColumns(): EntityTableColumns<TenantDepartment> {
    const { t } = useTranslation();

    return [
        {
            title: t('components.columns.tenantDepartment.departmentName'),
            dataIndex: "name",
            key: "name",
            render: (value: unknown) => String(value),
        },
        {
            title: t('components.columns.tenantDepartment.description'),
            dataIndex: "description",
            key: "description",
            render: (value: unknown) => value ? String(value) : "-",
        },
        {
            title: t('components.columns.tenantDepartment.parentDepartment'),
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
}
