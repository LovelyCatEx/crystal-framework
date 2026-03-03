import type {BaseEntity} from "./BaseEntity.ts";

export enum OAuthPlatform {
    GITHUB = 0
}

export interface OAuthAccount extends BaseEntity {
    userId: string | null;
    platform: number;
    identifier: string;
    nickname: string | null;
    avatar: string | null;
}
