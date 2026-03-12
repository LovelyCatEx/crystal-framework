import {ShopOutlined} from "@ant-design/icons";
import {forwardRef} from "react";
import {EntityIdSelector} from "./EntityIdSelector.tsx";
import {TENANT_MANAGER_TABLE_COLUMNS} from "../columns/TenantEntityColumns.tsx";
import {TenantManagerController} from "@/api/tenant.api.ts";
import type {Tenant} from "@/types/tenant.types.ts";
import type {EntityIdSelectorRef} from "./EntityIdSelector.tsx";

interface TenantIdSelectorProps {
    value?: string | null;
    onChange?: (value: string | null) => void;
    onEntityChange?: (tenant: Tenant | null) => void;
}

export const TenantIdSelector = forwardRef<EntityIdSelectorRef, TenantIdSelectorProps>(
    ({ value, onChange, onEntityChange }, ref) => {
        return (
            <EntityIdSelector<Tenant>
                ref={ref}
                value={value}
                onChange={onChange}
                onEntityChange={onEntityChange}
                entityName="租户"
                columns={TENANT_MANAGER_TABLE_COLUMNS}
                controller={TenantManagerController}
                displayRender={(tenant) => `${tenant.name} (${tenant.contactEmail})`}
                placeholder="选择租户"
                icon={<ShopOutlined />}
            />
        );
    }
);
