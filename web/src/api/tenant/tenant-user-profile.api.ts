import {doGet, doPost} from "../system-request.ts";
import type {TenantUserProfile} from "@/types/tenant/tenant-user-profile.types.ts";
import type {Gender} from "@/types/common/gender.types.ts";

/**
 * Self-service tenant-scoped user profile.
 * Backend: MyTenantUserProfileController (crystal-tenant, /me/tenant-profile).
 *
 * GET returns null when the current member has no profile row yet.
 * The upsert endpoint creates or updates by tenantMemberId.
 */

export function getMyTenantUserProfile() {
    return doGet<TenantUserProfile | null>('/api/me/tenant-profile');
}

export interface UpsertMyTenantUserProfileDTO {
    phone?: string;
    nickname?: string;
    email?: string;
    bio?: string;
    gender?: Gender;
    birthday?: string;
    timezone?: string;
    locale?: string;
}

export function upsertMyTenantUserProfile(dto: UpsertMyTenantUserProfileDTO) {
    return doPost<TenantUserProfile>('/api/me/tenant-profile/upsert', {...dto});
}

export function uploadMyTenantMemberAvatar(file: File) {
    return doPost<TenantUserProfile>(
        '/api/me/tenant-profile/uploadAvatar',
        {file},
        {'Content-Type': 'multipart/form-data'},
    );
}
