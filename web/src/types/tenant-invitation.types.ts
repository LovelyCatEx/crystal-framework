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
}