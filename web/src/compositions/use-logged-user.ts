import {useEffect, useState} from "react";
import {message} from "antd";
import type {UserProfileVO} from "../types/user.types.ts";
import {getUserAccessibleMenus, getUserProfile} from "../api/user.api.ts";
import type {ApiResponse} from "../api/system-request.ts";

export const useLoggedUser = () => {
    const [userProfile, setUserProfile] = useState<UserProfileVO | null>(null);
    const [accessibleMenuPaths, setAccessibleMenuPaths] = useState<string[]>([]);

    useEffect(() => {
        getUserProfile()
            .then((res: ApiResponse<UserProfileVO>) => {
                setUserProfile(res.data);
            })
            .catch(() => {
                void message.warning("无法获取用户信息")
            })

        getUserAccessibleMenus()
            .then((res: ApiResponse<string[]>) => {
                setAccessibleMenuPaths(res.data ?? [])
            })
            .catch(() => {
                void message.warning("无法获取资源列表")
            })
    }, []);

    return { userProfile, accessibleMenuPaths };
}