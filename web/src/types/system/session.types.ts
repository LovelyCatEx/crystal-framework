import type {BaseEntity} from "@/types/BaseEntity.ts";

export enum SessionType {
    USER = 0,
    PROMETHEUS = 1,
}

export interface SessionDescription extends BaseEntity {
    sessionId: string;
    remoteIp: string;
    userAgent: string;
    userId: string | null;
    tenantId: string | null;
    type: number;
}