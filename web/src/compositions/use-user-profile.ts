/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {useSWRState} from "@/compositions/use-swr.ts";
import type {UserProfileVO} from "@/types/user/user.types.ts";
import {getUserProfile} from "@/api/user/user.api.ts";
import {message} from "antd";

export const useUserProfile = (userId?: string | null) => {
    const [userProfile, , isUserProfileLoading, refreshUserProfile] = useSWRState<UserProfileVO>(
        userId ? `getUserProfile?id=${userId}` : undefined,
        () => getUserProfile(userId!),
        () => void message.error("无法获取用户资料")
    );

    return { userProfile, isUserProfileLoading, refreshUserProfile };
}

export const useCurrentUserProfile = (cacheIdentity?: string) => {
    const [userProfile, , isUserProfileLoading, refreshUserProfile] = useSWRState<UserProfileVO>(
        cacheIdentity ? ['getUserProfile', cacheIdentity] : undefined,
        getUserProfile,
        () => void message.error("无法获取用户资料")
    );

    return { userProfile, isUserProfileLoading, refreshUserProfile };
}