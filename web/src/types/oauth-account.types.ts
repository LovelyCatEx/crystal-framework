import type {BaseEntity} from "./BaseEntity.ts";

export enum OAuthPlatform {
    GITHUB = 0,
    GOOGLE = 1,
    OICQ = 2
}

export function getOAuthPlatformByName(platformName: string) {
    const name = platformName.toLowerCase();
    if (name === "github") {
        return OAuthPlatform.GITHUB;
    } else if (name === "google") {
        return OAuthPlatform.GOOGLE;
    } else if (name === "oicq") {
        return OAuthPlatform.OICQ;
    } else {
        return null;
    }
}

export interface OAuthAccount extends BaseEntity {
    userId: string | null;
    platform: number;
    identifier: string;
    nickname: string | null;
    avatar: string | null;
}
