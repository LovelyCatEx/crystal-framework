/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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
