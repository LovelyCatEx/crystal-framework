import type {BaseEntity} from "@/types/BaseEntity.ts";
import type {Gender} from "@/types/common/gender.types.ts";

/**
 * Tenant-scoped profile that overlays the system-level user profile.
 * Backend: TenantUserProfileEntity (crystal-tenant).
 *
 * Nullable overlay fields (nickname / avatar / email) fall back to the system-level
 * users.* values on display when null.
 */
export interface TenantUserProfile extends BaseEntity {
    tenantId: string;
    tenantMemberId: string;
    memberUserId: string;
    name: string;
    phone: string;
    nickname: string | null;
    avatar: string | null;
    email: string | null;
    bio: string | null;
    gender: Gender | null;
    birthday: string | null;
    timezone: string | null;
    locale: string | null;
}
