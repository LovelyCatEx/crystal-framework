/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import type {BaseEntity} from "@/types/BaseEntity.ts";

export interface Invitation extends BaseEntity {
    id: string;
    tenantId: string;
    creatorMemberId: string;
    departmentId?: string;
    invitationCode: string;
    invitationCount: number;
    expiresTime?: string;
    requiresReviewing: boolean;
    createdTime: string;
    modifiedTime: string;
}

export interface TenantInvitationVO {
    tenantId: string;
    expiresAt?: string;
    departmentName?: string;
    reachedUsageLimit: boolean;
}