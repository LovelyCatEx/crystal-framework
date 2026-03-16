import {useSWRState} from "@/compositions/swr.ts";
import {message} from "antd";
import {getJoinedTenants} from "@/api/tenant.api.ts";
import {useMemo} from "react";
import {getTenantProfile} from "@/api/tenant-profile.api.ts";

export const useUserTenants = () => {
    const [joinedTenants, , isJoinedTenantsLoading, refreshJoinedTenants] = useSWRState(
        'getJoinedTenants',
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