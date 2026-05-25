import type {BaseEntity} from "@/types/BaseEntity.ts";

export interface SessionDescription extends BaseEntity {
    sessionId: string;
    remoteIp: string;
    userAgent: string;
    userId: number;
    tenantId: number;
}