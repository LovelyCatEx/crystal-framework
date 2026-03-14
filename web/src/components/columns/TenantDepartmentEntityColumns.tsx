import type {EntityTableColumns} from "../types/entity-table.types.ts";
import type {TenantDepartment} from "@/types/tenant-department.types.ts";

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
        title: "父部门ID",
        dataIndex: "parentId",
        key: "parentId",
        render: (value: unknown) => value ? String(value) : "-",
    },
];
