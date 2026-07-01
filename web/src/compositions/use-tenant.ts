import {useSWRState} from "@/compositions/use-swr.ts";
import {message} from "antd";
import {getJoinedTenants} from "@/api/tenant/tenant.api.ts";
import {useMemo} from "react";
import {getTenantProfile} from "@/api/tenant/tenant-profile.api.ts";
import {useSystemIntegrated} from "@/context/SystemIntegratedContext.tsx";
import {isModuleDisabled, SystemModuleKey} from "@/router/system-module-menu-paths.ts";

export const useUserTenants = () => {
    const {disabledModules} = useSystemIntegrated();
    const tenantModuleDisabled = isModuleDisabled(disabledModules, SystemModuleKey.TENANT);

    // Suppress the /tenant/joined request entirely when the tenant module is disabled —
    // the backend Filter would otherwise reject it with a BusinessException.
    const [joinedTenants, , isJoinedTenantsLoading, refreshJoinedTenants] = useSWRState(
        tenantModuleDisabled ? undefined : 'getJoinedTenants',
        getJoinedTenants,
        () => void message.error("无法获取已加入的组织")
    );

    const currentTenant = useMemo(() => {
        return joinedTenants?.find((it) => it.authenticated) ?? null;
    }, [joinedTenants]);

    const currentTenantProfile = useSWRState(
        currentTenant ? 'currentTenantProfile' : undefined,
        getTenantProfile,
        () => void message.error("无法获取组织信息")
    );

    return { joinedTenants, isJoinedTenantsLoading, refreshJoinedTenants, currentTenant, currentTenantProfile };
}