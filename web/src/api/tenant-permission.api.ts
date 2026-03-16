import {BaseManagerController} from "./BaseManagerController.ts";
import type {BaseManagerDeleteDTO, BaseManagerUpdateDTO} from "../types/api.types.ts";
import type {TenantPermission} from "@/types/tenant-permission.types.ts";

export interface ManagerCreateTenantPermissionDTO {
    name: string;
    description?: string;
    type: number;
    path?: string;
}

export interface ManagerUpdateTenantPermissionDTO extends BaseManagerUpdateDTO {
    name?: string;
    description?: string;
    type?: number;
    path?: string;
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

class TenantPermissionManagerControllerClass extends BaseManagerController<
    TenantPermission,
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
