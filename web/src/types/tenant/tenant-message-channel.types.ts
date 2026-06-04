import type {BaseEntity} from "@/types/BaseEntity.ts";

export enum ChannelType {
    EMAIL = 1,
    LARK = 2
}

export interface TenantMessageChannel extends BaseEntity {
    tenantId: string;
    channelType: number;
    name: string;
    enabled: boolean;
    config: string;
}
