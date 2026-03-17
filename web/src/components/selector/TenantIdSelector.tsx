import {ShopOutlined} from "@ant-design/icons";
import {forwardRef} from "react";
import {useTranslation} from "react-i18next";
import {EntityIdSelector} from "./EntityIdSelector.tsx";
import {useTenantTableColumns} from "../columns/TenantEntityColumns.tsx";
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
        const { t } = useTranslation();
        const columns = useTenantTableColumns();
        
        return (
            <EntityIdSelector<Tenant>
                ref={ref}
                value={value}
                onChange={onChange}
                onEntityChange={onEntityChange}
                entityName={t('entityNames.tenant')}
                columns={columns}
                controller={TenantManagerController}
                displayRender={(tenant) => `${tenant.name} (${tenant.contactEmail})`}
                placeholder={t('components.selector.entityIdSelector.placeholder')}
                icon={<ShopOutlined />}
            />
        );
    }
);
