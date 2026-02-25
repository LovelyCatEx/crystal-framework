import type {UserProfileVO} from "../types/user.types.ts";
import {getUserAccessibleMenus, getUserProfile} from "../api/user.api.ts";
import {useSWRState} from "./swr.ts";
import {message} from "antd";

export const useLoggedUser = () => {
    const [userProfile] = useSWRState<UserProfileVO>(
        'getUserProfile',
        getUserProfile,
        () => void message.error("无法获取用户资料")
    );

    const [accessibleMenuPaths] = useSWRState<string[]>(
        'getUserAccessibleMenus',
        getUserAccessibleMenus,
        () => void message.error("无法获取资源列表")
    );

    return { userProfile, accessibleMenuPaths };
}