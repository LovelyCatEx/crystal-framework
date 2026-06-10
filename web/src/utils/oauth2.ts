import {currentEnvironment} from "../config/env.ts";
import type {OAuthPlatform} from "@/types/user/oauth-account.types.ts";
import {OAuthBindingScope} from "@/types/user/oauth-account.types.ts";
import {PLATFORM_REGISTRATION_ID_MAP} from "@/global/constants.ts";

export function getOAuth2LoginUrl(platform: string) {
    const baseUrl = currentEnvironment.baseUrl;
    const cleanBaseUrl = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl;

    return `${cleanBaseUrl}/oauth2/authorization/${platform}`;
}

/**
 * Key used to store bind metadata in sessionStorage before OAuth redirect.
 * OAuth2CodePage reads this to dispatch to the bind page after callback.
 */
export const OAUTH_BIND_META_KEY = 'oauth-bind-meta';

export interface OAuthBindMeta {
    registrationId: string;
    scope: OAuthBindingScope;
}

/**
 * Initiates an OAuth flow for account binding (system-level or tenant-level).
 * Stores bind metadata in sessionStorage, then redirects to the OAuth provider.
 * After callback, OAuth2CodePage dispatches to OAuth2BindPage with the code.
 */
export function redirectToOAuthBind(platform: OAuthPlatform, scope: OAuthBindingScope) {
    const registrationId = PLATFORM_REGISTRATION_ID_MAP[platform];
    const meta: OAuthBindMeta = { registrationId, scope };
    sessionStorage.setItem(OAUTH_BIND_META_KEY, JSON.stringify(meta));
    window.location.href = getOAuth2LoginUrl(registrationId);
}
