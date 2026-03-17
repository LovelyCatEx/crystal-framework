import {SafetyCertificateOutlined} from "@ant-design/icons";
import {forwardRef} from "react";
import {useTranslation} from "react-i18next";
import {EntityIdSelector} from "./EntityIdSelector.tsx";
import {useTenantRoleTableColumns} from "../columns/TenantRoleEntityColumns.tsx";
import {TenantRoleManagerController} from "@/api/tenant-role.api.ts";
import type {EntityIdSelectorRef} from "./EntityIdSelector.tsx";
import type {TenantRole} from "@/types/tenat-role.types.ts";

interface TenantRoleIdSelectorProps {
    tenantId: string;
    value?: string | null;
    onChange?: (value: string | null) => void;
    disabledRoleId?: string | null;
}

export const TenantRoleIdSelector = forwardRef<EntityIdSelectorRef, TenantRoleIdSelectorProps>(
    ({ tenantId, value, onChange, disabledRoleId }, ref) => {
        const { t } = useTranslation();
        const columns = useTenantRoleTableColumns();
        
        return (
            <EntityIdSelector<TenantRole>
                ref={ref}
                value={value}
                onChange={onChange}
                entityName={t('entityNames.tenantRole')}
                columns={columns}
                controller={TenantRoleManagerController}
                displayRender={(role) => `${role.name}`}
                isRowDisabled={(role) => role.id === disabledRoleId}
                placeholder={t('components.selector.entityIdSelector.placeholder')}
                icon={<SafetyCertificateOutlined />}
                additionalQueryParams={() => {
                    return { tenantId: tenantId }
                }}
            />
        );
    }
);
