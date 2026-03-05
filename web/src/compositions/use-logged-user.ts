import type {UserProfileVO} from "../types/user.types.ts";
import {getUserAccessibleMenus, getUserProfile} from "../api/user.api.ts";
import {useSWRState} from "./swr.ts";
import {message} from "antd";
import {useMemo} from "react";
import {getUserAuthentication} from "../utils/token.utils.ts";

export const useLoggedUser = () => {
    const hasAuthToken = useMemo(() => {
        const auth = getUserAuthentication();
        return !!auth && !auth.expired;
    }, []);

    const [userProfile, , , refreshUserProfile] = useSWRState<UserProfileVO>(
        hasAuthToken ? 'getUserProfile' : undefined,
        getUserProfile,
        () => void message.error("无法获取用户资料")
    );

    const [accessibleMenuPaths] = useSWRState<string[]>(
        hasAuthToken ? 'getUserAccessibleMenus' : undefined,
        getUserAccessibleMenus,
        () => void message.error("无法获取资源列表")
    );

    return { userProfile, accessibleMenuPaths, refreshUserProfile, hasAuthToken };
}