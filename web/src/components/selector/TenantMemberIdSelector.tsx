import {UserOutlined} from "@ant-design/icons";
import {forwardRef} from "react";
import {useTranslation} from "react-i18next";
import {EntityIdSelector} from "./EntityIdSelector.tsx";
import {useTenantMemberTableColumns} from "../columns/TenantMemberEntityColumns.tsx";
import {TenantMemberManagerController} from "@/api/tenant-member.api.ts";
import type {EntityIdSelectorRef} from "./EntityIdSelector.tsx";
import type {TenantMemberVO} from "@/types/tenant-member.types.ts";

interface TenantMemberIdSelectorProps {
    value?: string | null;
    onChange?: (value: string | null) => void;
    tenantId: string;
    placeholder?: string;
}

export const TenantMemberIdSelector = forwardRef<EntityIdSelectorRef, TenantMemberIdSelectorProps>(
    ({ value, onChange, tenantId, placeholder }, ref) => {
        const { t } = useTranslation();
        const columns = useTenantMemberTableColumns();
        
        return (
            <EntityIdSelector<TenantMemberVO>
                ref={ref}
                value={value}
                onChange={onChange}
                entityName={t('entityNames.tenantMember')}
                columns={columns}
                controller={TenantMemberManagerController}
                displayRender={(member) => `${member.user?.username || ''} (${member.user?.email || ''})`}
                placeholder={placeholder ?? t('components.selector.entityIdSelector.placeholder')}
                icon={<UserOutlined />}
                additionalQueryParams={() => {
                    return { tenantId: tenantId };
                }}
            />
        );
    }
);
