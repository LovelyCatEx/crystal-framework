import type {BaseEntity} from "@/types/BaseEntity.ts";

/** Isolation boundary of a broadcast — mirrors backend sdk ScopeType.kt (typeId). */
export enum ScopeType {
    SYSTEM = 0,
    TENANT = 1,
}

/** Outward-facing sender identity — mirrors backend sdk PartyType.kt (typeId). */
export enum PartyType {
    USER = 0,
    SYSTEM = 1,
    TENANT = 2,
}

/** How a broadcast's audience is defined — mirrors backend sdk AudienceType.kt (typeId). */
export enum AudienceType {
    ALL_USERS = 0,
    TENANT_MEMBERS = 1,
    SEGMENT = 2,
}

/** Business discriminator of a broadcast — mirrors backend message BroadcastCategory.kt (typeId). */
export enum BroadcastCategory {
    ANNOUNCEMENT = 0,
}

/**
 * Serialized {@link MsgBroadcastEntity}. All backend `Long` fields arrive as `string`
 * (ToStringSerializer). `scopeType` / `senderPartyType` / `category` / `audienceType`
 * are enum typeIds (`number`).
 */
export interface Broadcast extends BaseEntity {
    scopeType: number;
    scopeId: string | null;
    senderPartyType: number;
    senderPartyId: string | null;
    actingUserId: string | null;
    category: number;
    audienceType: number;
    audienceRef: string | null;
    title: string;
    content: string;
    publishTime: string;
    expireTime: string | null;
}
