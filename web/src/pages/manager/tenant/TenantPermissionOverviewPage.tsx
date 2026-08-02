import {StandardCard} from "@/components/card/StandardCard.tsx";
import {PermissionTreeTable} from "@/components/PermissionTreeTable.tsx";
import {useProtectedController} from "@/components/base/ProtectedControllerWarningWrapper.tsx";
import {useSWRState} from "@/compositions/use-swr.ts";
import {getTenantPermissionType} from "@/i18n/enum-helpers.ts";
import type {TenantPermission} from "@/types/tenant/rbac/tenant-permission.types.ts";
import type {
    ManagerCreateTenantPermissionDTO,
    ManagerReadTenantPermissionDTO,
    ManagerUpdateTenantPermissionDTO,
} from "@/api/tenant/rbac/tenant-permission.api.ts";

const SWR_KEY = "manager.tenant-permission.tree";

export default function TenantPermissionOverviewPage() {
    const {controller} = useProtectedController<
        TenantPermission,
        ManagerCreateTenantPermissionDTO,
        ManagerReadTenantPermissionDTO,
        ManagerUpdateTenantPermissionDTO
    >();

    const [permissions, , loading] = useSWRState<TenantPermission[]>(
        SWR_KEY,
        () => controller.list(),
    );

    return (
        <StandardCard>
            <PermissionTreeTable
                permissions={permissions ?? []}
                loading={loading}
                typeLabel={getTenantPermissionType}
            />
        </StandardCard>
    );
}
