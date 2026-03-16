import {UserOutlined} from "@ant-design/icons";
import {forwardRef} from "react";
import {EntityIdSelector} from "./EntityIdSelector.tsx";
import {TENANT_MEMBER_TABLE_COLUMNS} from "../columns/TenantMemberEntityColumns.tsx";
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
        return (
            <EntityIdSelector<TenantMemberVO>
                ref={ref}
                value={value}
                onChange={onChange}
                entityName="租户成员"
                columns={TENANT_MEMBER_TABLE_COLUMNS}
                controller={TenantMemberManagerController}
                displayRender={(member) => `${member.user?.username || ''} (${member.user?.email || ''})`}
                placeholder={placeholder ?? "选择租户成员"}
                icon={<UserOutlined />}
                additionalQueryParams={() => {
                    return { tenantId: tenantId };
                }}
            />
        );
    }
);
