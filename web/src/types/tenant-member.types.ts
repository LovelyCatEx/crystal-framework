import type {BaseEntity} from "@/types/BaseEntity.ts";
import type {User} from "@/types/user.types.ts";

export enum TenantMemberStatus {
    INACTIVE = 0,
    DEPARTED = 1,
    RESIGNED = 2,
    REVIEWING = 3,
    ACTIVE = 4
}

export interface TenantMember extends BaseEntity {
    id: string;
    tenantId: string;
    memberUserId: string;
    status: number;
    createdTime: string;
    modifiedTime: string;
}

export interface TenantMemberVO extends TenantMember {
    user: User;
}