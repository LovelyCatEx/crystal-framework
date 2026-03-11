import {doGet, doPost} from "./system-request.ts";
import type {TenantDepartmentMemberVO} from "@/types/tenant-department-member.types.ts";

export async function getDepartmentMembers(departmentId: string) {
    return doGet<TenantDepartmentMemberVO[]>('/api/manager/tenant/department/member/get', { departmentId });
}

export async function setDepartmentMembers(departmentId: string, memberIds: string[]) {
    return doPost<unknown>('/api/manager/tenant/department/member/set', { departmentId, memberIds }, { 'Content-Type': 'application/json' });
}
