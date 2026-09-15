/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {doGet, doPost} from "../../system-request.ts";
import type {TenantRole} from "@/types/tenant/rbac/tenant-role.types.ts";

export async function getTenantMemberRoles(memberId: string) {
    return doGet<TenantRole[]>('/api/manager/tenant/member/role/get', { memberId });
}

export async function setTenantMemberRoles(memberId: string, roleIds: string[]) {
    return doPost<unknown>('/api/manager/tenant/member/role/set', { memberId, roleIds }, { 'Content-Type': 'application/json' });
}
