import {currentEnvironment} from "../config/env.ts";

export function getOAuth2LoginUrl(platform: string) {
    const baseUrl = currentEnvironment.baseUrl;
    const cleanBaseUrl = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl;

    return `${cleanBaseUrl}/oauth2/authorization/${platform}`;
}