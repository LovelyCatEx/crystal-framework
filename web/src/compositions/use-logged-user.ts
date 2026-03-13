import {getUserAccessibleMenus} from "../api/user.api.ts";
import {useSWRState} from "./swr.ts";
import {message} from "antd";
import {useMemo} from "react";
import {getUserAuthentication} from "../utils/token.utils.ts";
import {useUserProfile} from "@/compositions/use-user-profile.ts";

export const useLoggedUser = () => {
    const hasAuthToken = useMemo(() => {
        const auth = getUserAuthentication();
        return !!auth && !auth.expired;
    }, []);

    const { userProfile, refreshUserProfile } = useUserProfile();

    const [accessibleMenuPaths] = useSWRState<string[]>(
        hasAuthToken ? 'getUserAccessibleMenus' : undefined,
        getUserAccessibleMenus,
        () => void message.error("无法获取资源列表")
    );

    return { userProfile, accessibleMenuPaths, refreshUserProfile, hasAuthToken };
}