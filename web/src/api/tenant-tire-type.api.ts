import {BaseManagerController} from "./BaseManagerController.ts";
import type {TenantTireType} from "../types/tenant.types.ts";
import type {BaseManagerReadDTO, BaseManagerUpdateDTO} from "../types/api.types.ts";

export const TenantTireTypeManagerController = new BaseManagerController<
    TenantTireType,
    ManagerCreateTenantTireTypeDTO,
    ManagerReadTenantTireTypeDTO,
    ManagerUpdateTenantTireTypeDTO
>('/manager/tenant/tire');

export interface ManagerCreateTenantTireTypeDTO {
    name: string;
    description: string | null;
}

export interface ManagerUpdateTenantTireTypeDTO extends BaseManagerUpdateDTO {
    name?: string | null;
    description?: string | null;
}

export interface ManagerReadTenantTireTypeDTO extends BaseManagerReadDTO {
}
