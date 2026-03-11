import {BaseManagerController} from "./BaseManagerController.ts";
import type {BaseManagerDeleteDTO, BaseManagerUpdateDTO} from "../types/api.types.ts";
import type {TenantRole} from "@/types/tenat-role.types.ts";

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
    TenantRole,
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
