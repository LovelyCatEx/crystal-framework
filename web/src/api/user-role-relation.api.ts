import {doGet, doPost} from "./system-request.ts";
import type {UserRole} from "../types/user-role.types.ts";

export async function getUserRoles(userId: string) {
    return doGet<UserRole[]>('/api/manager/user-role-relation/get', { userId });
}

export async function setUserRoles(userId: string, roleIds: string[]) {
    return doPost<unknown>('/api/manager/user-role-relation/set', { userId, roleIds }, { 'Content-Type': 'application/json' });
}
