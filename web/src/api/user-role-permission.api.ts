import {doGet, doPost} from "./system-request.ts";
import type {UserPermission} from "../types/user-permission.types.ts";

export async function getRolePermissions(roleId: string) {
    return doGet<UserPermission[]>('/api/manager/user-role-permission/get', { roleId });
}

export async function setRolePermissions(roleId: string, permissionIds: string[]) {
    return doPost<unknown>('/api/manager/user-role-permission/set', { roleId, permissionIds }, { 'Content-Type': 'application/json' });
}
