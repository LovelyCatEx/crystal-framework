import {ApartmentOutlined} from "@ant-design/icons";
import {forwardRef} from "react";
import {EntityIdSelector} from "./EntityIdSelector.tsx";
import {TENANT_DEPARTMENT_TABLE_COLUMNS} from "../columns/TenantDepartmentEntityColumns.tsx";
import {TenantDepartmentManagerController} from "@/api/tenant-department.api.ts";
import type {EntityIdSelectorRef} from "./EntityIdSelector.tsx";
import type {TenantDepartment} from "@/types/tenant-department.types.ts";

interface TenantDepartmentIdSelectorProps {
    value?: string | null;
    onChange?: (value: string | null) => void;
    disabledDepartmentId?: string | null;
    tenantId: string;
    placeholder?: string;
}

export const TenantDepartmentIdSelector = forwardRef<EntityIdSelectorRef, TenantDepartmentIdSelectorProps>(
    ({ value, onChange, disabledDepartmentId, tenantId, placeholder }, ref) => {
        return (
            <EntityIdSelector<TenantDepartment>
                ref={ref}
                value={value}
                onChange={onChange}
                entityName="租户部门"
                columns={TENANT_DEPARTMENT_TABLE_COLUMNS}
                controller={TenantDepartmentManagerController}
                displayRender={(dept) => `${dept.name}${dept.description ? ` (${dept.description})` : ''}`}
                isRowDisabled={(dept) => dept.id === disabledDepartmentId}
                placeholder={placeholder ?? "选择父部门（可选）"}
                icon={<ApartmentOutlined />}
                additionalQueryParams={() => {
                    return { tenantId: tenantId };
                }}
            />
        );
    }
);
