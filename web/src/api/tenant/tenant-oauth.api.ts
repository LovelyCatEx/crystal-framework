/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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
    oauthBindToken: string;
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
