/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {doGet, doPost} from "../../system-request.ts";
import type {UserPermission} from "@/types/user/rbac/user-permission.types.ts";

export async function getRolePermissions(roleId: string) {
    return doGet<UserPermission[]>('/api/manager/user-role-permission/get', { roleId });
}

export async function setRolePermissions(roleId: string, permissionIds: string[]) {
    return doPost<unknown>('/api/manager/user-role-permission/set', { roleId, permissionIds }, { 'Content-Type': 'application/json' });
}
