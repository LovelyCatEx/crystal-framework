import {useSWRState} from "@/compositions/swr.ts";
import type {UserProfileVO} from "@/types/user.types.ts";
import {getUserProfile} from "@/api/user.api.ts";
import {message} from "antd";

export const useUserProfile = (userId?: string) => {
    const [userProfile, , isUserProfileLoading, refreshUserProfile] = useSWRState<UserProfileVO>(
        `getUserProfile?id=${userId}`,
        getUserProfile,
        () => void message.error("无法获取用户资料")
    );

    return { userProfile, isUserProfileLoading, refreshUserProfile };
}