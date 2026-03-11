import {BaseManagerController} from "./BaseManagerController.ts";
import type {BaseManagerDeleteDTO, BaseManagerUpdateDTO} from "../types/api.types.ts";

export interface TenantPermissionVO {
    id: string;
    name: string;
    description: string | null;
    type: number;
    path: string | null;
    preserved1: number | null;
    preserved2: number | null;
    createdTime: string;
    modifiedTime: string;
}

export interface ManagerCreateTenantPermissionDTO {
    name: string;
    description?: string;
    type: number;
    path?: string;
    preserved1?: number;
    preserved2?: number;
}

export interface ManagerUpdateTenantPermissionDTO extends BaseManagerUpdateDTO {
    name?: string;
    description?: string;
    type?: number;
    path?: string;
    preserved1?: number;
    preserved2?: number;
}

export interface ManagerReadTenantPermissionDTO {
    searchKeyword?: string;
    type?: number;
    page: number;
    pageSize: number;
}

export interface ManagerDeleteTenantPermissionDTO extends BaseManagerDeleteDTO {
    ids: string[];
}

export const TenantPermissionType = {
    ACTION: 0,
    MENU: 1
} as const;

export const TenantPermissionTypeMap: Record<number, { label: string; color: string }> = {
    [TenantPermissionType.ACTION]: { label: '操作权限', color: 'blue' },
    [TenantPermissionType.MENU]: { label: '菜单权限', color: 'green' }
};

class TenantPermissionManagerControllerClass extends BaseManagerController<
    TenantPermissionVO,
    ManagerCreateTenantPermissionDTO,
    ManagerReadTenantPermissionDTO,
    ManagerUpdateTenantPermissionDTO,
    ManagerDeleteTenantPermissionDTO
> {
    constructor() {
        super('/manager/tenant/permission');
    }
}

export const TenantPermissionManagerController = new TenantPermissionManagerControllerClass();
