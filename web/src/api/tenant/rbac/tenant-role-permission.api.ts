/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {doGet, doPost} from "../../system-request.ts";
import type {TenantPermission} from "@/types/tenant/rbac/tenant-permission.types.ts";

export async function getTenantRolePermissions(roleId: string) {
    return doGet<TenantPermission[]>('/api/manager/tenant/role/permission/get', { roleId });
}

export async function setTenantRolePermissions(roleId: string, permissionIds: string[]) {
    return doPost<unknown>('/api/manager/tenant/role/permission/set', { roleId, permissionIds }, { 'Content-Type': 'application/json' });
}
