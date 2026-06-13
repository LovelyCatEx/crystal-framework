import {useTranslation} from "react-i18next";
import {EntityIdsSelector} from "./EntityIdsSelector.tsx";
import {useTenantMemberTableColumns} from "../columns/TenantMemberEntityColumns.tsx";
import {TenantMemberManagerController} from "@/api/tenant/tenant-member.api.ts";
import type {TenantMemberVO} from "@/types/tenant/tenant-member.types.ts";
import {UserChip} from "@/components/UserChip.tsx";

interface TenantMemberIdsSelectorProps {
    tenantId: string;
    value?: string[] | null;
    onChange?: (value: string[]) => void;
}

export function TenantMemberIdsSelector({ tenantId, value, onChange }: TenantMemberIdsSelectorProps) {
    const { t } = useTranslation();
    const columns = useTenantMemberTableColumns();

    return (
        <EntityIdsSelector<TenantMemberVO>
            value={value}
            onChange={onChange}
            entityName={t('entityNames.tenantMember')}
            columns={columns}
            query={async (params) => (await TenantMemberManagerController.query({ ...params, tenantId })).data!}
            getById={(id) => TenantMemberManagerController.getById(id, { tenantId })}
            renderItem={(member) => (
                <UserChip
                    userId={member.memberUserId}
                    avatar={member.user?.avatar}
                    name={member.user?.nickname || member.user?.username || member.memberUserId}
                />
            )}
        />
    );
}
