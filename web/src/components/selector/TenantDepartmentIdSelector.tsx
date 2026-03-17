import {ApartmentOutlined} from "@ant-design/icons";
import {forwardRef} from "react";
import {useTranslation} from "react-i18next";
import {EntityIdSelector} from "./EntityIdSelector.tsx";
import {useTenantDepartmentTableColumns} from "../columns/TenantDepartmentEntityColumns.tsx";
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
        const { t } = useTranslation();
        const columns = useTenantDepartmentTableColumns();
        
        return (
            <EntityIdSelector<TenantDepartment>
                ref={ref}
                value={value}
                onChange={onChange}
                entityName={t('entityNames.tenantDepartment')}
                columns={columns}
                controller={TenantDepartmentManagerController}
                displayRender={(dept) => `${dept.name}${dept.description ? ` (${dept.description})` : ''}`}
                isRowDisabled={(dept) => dept.id === disabledDepartmentId}
                placeholder={placeholder ?? t('components.selector.entityIdSelector.placeholder')}
                icon={<ApartmentOutlined />}
                additionalQueryParams={() => {
                    return { tenantId: tenantId };
                }}
            />
        );
    }
);
