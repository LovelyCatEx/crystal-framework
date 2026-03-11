import {BaseManagerController} from "./BaseManagerController.ts";
import type {BaseManagerDeleteDTO, BaseManagerUpdateDTO} from "../types/api.types.ts";

export interface TenantRoleVO {
    id: string;
    tenantId: string;
    name: string;
    description: string | null;
    parentId: string | null;
    createdTime: string;
    modifiedTime: string;
}

export interface ManagerCreateTenantRoleDTO {
    tenantId: string;
    name: string;
    description?: string;
    parentId?: string;
}

export interface ManagerUpdateTenantRoleDTO extends BaseManagerUpdateDTO {
    name?: string;
    description?: string;
    parentId?: string;
}

export interface ManagerReadTenantRoleDTO {
    tenantId?: string;
    searchKeyword?: string;
    parentId?: string;
    page: number;
    pageSize: number;
}

export interface ManagerDeleteTenantRoleDTO extends BaseManagerDeleteDTO {
    ids: string[];
}

class TenantRoleManagerControllerClass extends BaseManagerController<
    TenantRoleVO,
    ManagerCreateTenantRoleDTO,
    ManagerReadTenantRoleDTO,
    ManagerUpdateTenantRoleDTO,
    ManagerDeleteTenantRoleDTO
> {
    constructor() {
        super('/manager/tenant/role');
    }
}

export const TenantRoleManagerController = new TenantRoleManagerControllerClass();
