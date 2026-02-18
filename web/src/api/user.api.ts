import type {UserProfileVO} from "../types/user.types.ts";
import {type ApiResponse, doGet} from "./system-request.ts";

export function getUserProfile(userId?: number): Promise<ApiResponse<UserProfileVO>> {
    return doGet<UserProfileVO>('/api/user/profile', {userId});
}