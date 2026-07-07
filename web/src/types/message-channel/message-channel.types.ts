import type {BaseEntity} from "@/types/BaseEntity.ts";

export enum ChannelType {
    EMAIL = 1,
    LARK = 2
}

export interface MessageChannel extends BaseEntity {
    scope: number;
    scopeId: string;
    channelType: number;
    name: string;
    enabled: boolean;
    config: string;
}
