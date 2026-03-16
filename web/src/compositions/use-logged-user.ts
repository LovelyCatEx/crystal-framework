import {getUserAccessibleMenus} from "../api/user.api.ts";
import {useSWRState} from "./swr.ts";
import {message} from "antd";
import {useMemo} from "react";
import {getUserAuthentication} from "../utils/token.utils.ts";
import {useCurrentUserProfile} from "@/compositions/use-user-profile.ts";
import type {UserAccessibleResourceVO} from "@/types/user.types.ts";

export const useLoggedUser = () => {
    const hasAuthToken = useMemo(() => {
        const auth = getUserAuthentication();
        return !!auth && !auth.expired;
    }, []);

    const { userProfile, refreshUserProfile } = useCurrentUserProfile();

    const [accessibleResources] = useSWRState<UserAccessibleResourceVO>(
        hasAuthToken ? 'getUserAccessibleMenus' : undefined,
        getUserAccessibleMenus,
        () => void message.error("无法获取资源列表")
    );

    const accessibleMenuPaths = useMemo(() => {
        return accessibleResources?.menus ?? []
    }, [accessibleResources]);

    const accessibleComponentPaths = useMemo(() => {
        return accessibleResources?.components ?? []
    }, [accessibleResources]);

    return { userProfile, accessibleMenuPaths, accessibleComponentPaths, refreshUserProfile, hasAuthToken };
}