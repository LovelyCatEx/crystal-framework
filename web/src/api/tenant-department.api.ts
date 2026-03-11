import {BaseManagerController} from "./BaseManagerController.ts";
import type {BaseManagerDeleteDTO, BaseManagerUpdateDTO} from "../types/api.types.ts";
import type {TenantDepartment} from "@/types/tenant-department.types.ts";

export interface ManagerCreateTenantDepartmentDTO {
    tenantId: string;
    name: string;
    description?: string;
    parentId?: string;
}

export interface ManagerUpdateTenantDepartmentDTO extends BaseManagerUpdateDTO {
    name?: string;
    description?: string;
    parentId?: string;
}

export interface ManagerReadTenantDepartmentDTO {
    tenantId?: string;
    searchKeyword?: string;
    parentId?: string;
    page: number;
    pageSize: number;
}

export interface ManagerDeleteTenantDepartmentDTO extends BaseManagerDeleteDTO {
    ids: string[];
}

class TenantDepartmentManagerControllerClass extends BaseManagerController<
    TenantDepartment,
    ManagerCreateTenantDepartmentDTO,
    ManagerReadTenantDepartmentDTO,
    ManagerUpdateTenantDepartmentDTO,
    ManagerDeleteTenantDepartmentDTO
> {
    constructor() {
        super('/manager/tenant/department');
    }
}

export const TenantDepartmentManagerController = new TenantDepartmentManagerControllerClass();
