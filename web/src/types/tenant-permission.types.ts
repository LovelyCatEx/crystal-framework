import type {BaseEntity} from "@/types/BaseEntity.ts";

export interface TenantPermission extends BaseEntity {
    name: string;
    description: string | null;
    type: number;
    path: string | null;
}

export enum TenantPermissionType {
    ACTION = 0,
    MENU = 1
}

export const TenantPermissionTypeMap: Record<number, { label: string; color: string }> = {
    [TenantPermissionType.ACTION]: { label: '操作权限', color: 'blue' },
    [TenantPermissionType.MENU]: { label: '菜单权限', color: 'green' }
};