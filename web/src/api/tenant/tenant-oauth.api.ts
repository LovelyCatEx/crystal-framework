import {doGet, doPost} from "../system-request.ts";
import type {TenantOAuthAccount} from "@/types/tenant/tenant-oauth.types.ts";

/**
 * Tenant-scoped OAuth bindings for the current member.
 * Backend: TenantOAuthAccountController (crystal-auth, /tenant/oauth).
 */

export function getTenantOAuthAccounts() {
    return doGet<TenantOAuthAccount[]>('/api/tenant/oauth/accounts');
}

export interface BindTenantOAuthAccountDTO {
    oauthAccountId: string;
}

export function bindTenantOAuthAccount(dto: BindTenantOAuthAccountDTO) {
    return doPost<TenantOAuthAccount>('/api/tenant/oauth/bind', {...dto});
}

export interface UnbindTenantOAuthAccountDTO {
    oauthAccountId: string;
}

export function unbindTenantOAuthAccount(dto: UnbindTenantOAuthAccountDTO) {
    return doPost<unknown>('/api/tenant/oauth/unbind', {...dto});
}
