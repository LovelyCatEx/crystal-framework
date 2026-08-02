import {StandardCard} from "@/components/card/StandardCard.tsx";
import {PermissionTreeTable} from "@/components/PermissionTreeTable.tsx";
import {useProtectedController} from "@/components/base/ProtectedControllerWarningWrapper.tsx";
import {useSWRState} from "@/compositions/use-swr.ts";
import {getPermissionType} from "@/i18n/enum-helpers.ts";
import type {UserPermission} from "@/types/user/rbac/user-permission.types.ts";
import type {
    ManagerCreatePermissionDTO,
    ManagerReadPermissionDTO,
    ManagerUpdatePermissionDTO,
} from "@/api/user/rbac/user-permission.api.ts";

const SWR_KEY = "manager.user-permission.tree";

export default function UserPermissionOverviewPage() {
    const {controller} = useProtectedController<
        UserPermission,
        ManagerCreatePermissionDTO,
        ManagerReadPermissionDTO,
        ManagerUpdatePermissionDTO
    >();

    const [permissions, , loading] = useSWRState<UserPermission[]>(
        SWR_KEY,
        () => controller.list(),
    );

    return (
        <StandardCard>
            <PermissionTreeTable
                permissions={permissions ?? []}
                loading={loading}
                typeLabel={getPermissionType}
            />
        </StandardCard>
    );
}
