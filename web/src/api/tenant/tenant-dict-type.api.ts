import {BaseManagerController} from "../BaseManagerController.ts";
import type {BaseManagerDeleteDTO, BaseManagerReadScopedDTO, BaseManagerUpdateDTO} from "@/types/api.types.ts";
import type {TenantDictType} from "@/types/tenant/tenant-dict-type.types.ts";

export interface ManagerCreateTenantDictTypeDTO {
    scope: number;
    scopeId: string;
    code: string;
    name: string;
    remark?: string;
    status?: number;
}

export interface ManagerReadTenantDictTypeDTO extends BaseManagerReadScopedDTO {
}

export interface ManagerUpdateTenantDictTypeDTO extends BaseManagerUpdateDTO {
    name?: string;
    remark?: string;
    status?: number;
}

export interface ManagerDeleteTenantDictTypeDTO extends BaseManagerDeleteDTO {
    ids: string[];
}

class TenantDictTypeManagerControllerClass extends BaseManagerController<
    TenantDictType,
    ManagerCreateTenantDictTypeDTO,
    ManagerReadTenantDictTypeDTO,
    ManagerUpdateTenantDictTypeDTO,
    ManagerDeleteTenantDictTypeDTO
> {
    constructor() {
        super('/manager/tenant/dict-type');
    }
}

export const TenantDictTypeManagerController = new TenantDictTypeManagerControllerClass();
