import {doGet, doPost} from "./system-request.ts";
import type {TenantPermission} from "@/types/tenant-permission.types.ts";

export async function getTenantRolePermissions(roleId: string) {
    return doGet<TenantPermission[]>('/api/manager/tenant/role/permission/get', { roleId });
}

export async function setTenantRolePermissions(roleId: string, permissionIds: string[]) {
    return doPost<unknown>('/api/manager/tenant/role/permission/set', { roleId, permissionIds }, { 'Content-Type': 'application/json' });
}
