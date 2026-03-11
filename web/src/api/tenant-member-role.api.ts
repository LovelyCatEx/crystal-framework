import {doGet, doPost} from "./system-request.ts";
import type {TenantRole} from "@/types/tenat-role.types.ts";

export async function getTenantMemberRoles(memberId: string) {
    return doGet<TenantRole[]>('/api/manager/tenant/member/role/get', { memberId });
}

export async function setTenantMemberRoles(memberId: string, roleIds: string[]) {
    return doPost<unknown>('/api/manager/tenant/member/role/set', { memberId, roleIds }, { 'Content-Type': 'application/json' });
}
