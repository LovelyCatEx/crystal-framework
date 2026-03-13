import {useSWRState} from "@/compositions/swr.ts";
import {message} from "antd";
import {getJoinedTenants} from "@/api/tenant.api.ts";
import {useMemo} from "react";

export const useUserTenants = () => {
    const [joinedTenants, , isJoinedTenantsLoading, refreshJoinedTenants] = useSWRState(
        'getJoinedTenants',
        getJoinedTenants,
        () => void message.error("无法获取已加入的组织")
    );

    const currentTenant = useMemo(() => {
        return joinedTenants?.find((it) => it.authenticated) ?? null;
    }, [joinedTenants]);

    return { joinedTenants, isJoinedTenantsLoading, refreshJoinedTenants, currentTenant };
}