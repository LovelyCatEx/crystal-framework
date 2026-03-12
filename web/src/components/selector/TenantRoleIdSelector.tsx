import {SafetyCertificateOutlined} from "@ant-design/icons";
import {forwardRef} from "react";
import {EntityIdSelector} from "./EntityIdSelector.tsx";
import {TENANT_ROLE_TABLE_COLUMNS} from "../columns/TenantRoleEntityColumns.tsx";
import {TenantRoleManagerController} from "@/api/tenant-role.api.ts";
import type {EntityIdSelectorRef} from "./EntityIdSelector.tsx";
import type {TenantRole} from "@/types/tenat-role.types.ts";

interface TenantRoleIdSelectorProps {
    value?: string | null;
    onChange?: (value: string | null) => void;
    disabledRoleId?: string | null;
}

export const TenantRoleIdSelector = forwardRef<EntityIdSelectorRef, TenantRoleIdSelectorProps>(
    ({ value, onChange, disabledRoleId }, ref) => {
        return (
            <EntityIdSelector<TenantRole>
                ref={ref}
                value={value}
                onChange={onChange}
                entityName="租户角色"
                columns={TENANT_ROLE_TABLE_COLUMNS}
                controller={TenantRoleManagerController}
                displayRender={(role) => `${role.name}${role.description ? ` (${role.description})` : ''}`}
                isRowDisabled={(role) => role.id === disabledRoleId}
                placeholder="选择父角色（可选）"
                icon={<SafetyCertificateOutlined />}
            />
        );
    }
);
