/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {doGet, doPost} from "../../system-request.ts";
import type {UserRole} from "@/types/user/rbac/user-role.types.ts";

export async function getUserRoles(userId: string) {
    return doGet<UserRole[]>('/api/manager/user-role-relation/get', { userId });
}

export async function setUserRoles(userId: string, roleIds: string[]) {
    return doPost<unknown>('/api/manager/user-role-relation/set', { userId, roleIds }, { 'Content-Type': 'application/json' });
}
