import {ApartmentOutlined} from "@ant-design/icons";
import {forwardRef} from "react";
import {EntityIdSelector} from "./EntityIdSelector.tsx";
import {TENANT_DEPARTMENT_TABLE_COLUMNS} from "../columns/TenantDepartmentEntityColumns.tsx";
import {TenantDepartmentManagerController, type TenantDepartmentVO} from "@/api/tenant-department.api.ts";
import type {EntityIdSelectorRef} from "./EntityIdSelector.tsx";

interface TenantDepartmentIdSelectorProps {
    value?: string | null;
    onChange?: (value: string | null) => void;
    disabledDepartmentId?: string | null;
}

export const TenantDepartmentIdSelector = forwardRef<EntityIdSelectorRef, TenantDepartmentIdSelectorProps>(
    ({ value, onChange, disabledDepartmentId }, ref) => {
        return (
            <EntityIdSelector<TenantDepartmentVO>
                ref={ref}
                value={value}
                onChange={onChange}
                entityName="租户部门"
                columns={TENANT_DEPARTMENT_TABLE_COLUMNS}
                controller={TenantDepartmentManagerController}
                displayRender={(dept) => `${dept.name}${dept.description ? ` (${dept.description})` : ''}`}
                isRowDisabled={(dept) => dept.id === disabledDepartmentId}
                placeholder="选择父部门（可选）"
                icon={<ApartmentOutlined />}
            />
        );
    }
);
