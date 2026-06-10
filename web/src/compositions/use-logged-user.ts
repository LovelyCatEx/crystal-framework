import {getUserAccessibleMenus} from "../api/user/user.api.ts";
import {useSWRComposition} from "./use-swr.ts";
import type {ApiResponse} from "@/api/system-request.ts";
import {message} from "antd";
import {getUserAuthentication} from "../utils/token.utils.ts";
import {useCurrentUserProfile} from "@/compositions/use-user-profile.ts";
import type {UserAccessibleResourceVO} from "@/types/user/user.types.ts";

export const useLoggedUser = () => {
    const auth = getUserAuthentication();
    const hasAuthToken = !!auth && !auth.expired;

    const { userProfile, refreshUserProfile } = useCurrentUserProfile(hasAuthToken);

    const { data: accessibleResourcesResponse, isLoading: isAccessibleMenusLoading } = useSWRComposition<ApiResponse<UserAccessibleResourceVO>>(
        hasAuthToken ? 'getUserAccessibleMenus' : undefined,
        getUserAccessibleMenus,
        () => void message.error("无法获取资源列表")
    );

    const accessibleResources = accessibleResourcesResponse?.data ?? null;
    const accessibleMenuPaths = accessibleResources?.menus ?? [];
    const accessibleComponentPaths = accessibleResources?.components ?? [];

    return { userProfile, accessibleMenuPaths, accessibleComponentPaths, refreshUserProfile, hasAuthToken, isAccessibleMenusLoading };
}