/**
 * A tenant-scoped OAuth binding owned by the current member.
 * Backend: TenantOAuthAccountVO (crystal-auth).
 */
export interface TenantOAuthAccount {
    id: string;
    platformId: number;
    scope: number;
    tenantId: string | null;
    nickname: string | null;
    avatar: string | null;
}
