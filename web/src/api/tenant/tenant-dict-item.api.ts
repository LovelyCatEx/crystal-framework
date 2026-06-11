import {BaseManagerController} from "../BaseManagerController.ts";
import type {BaseManagerDeleteDTO, BaseManagerUpdateDTO} from "@/types/api.types.ts";
import type {TenantDictItem, TenantDictItemTreeNode} from "@/types/tenant/tenant-dict-item.types.ts";
import {doGet} from "@/api/system-request.ts";

export interface ManagerCreateTenantDictItemDTO {
    typeId: string;
    itemCode: string;
    itemValue: string;
    parentId?: string;
    sortOrder?: number;
    isDefault?: boolean;
    status?: number;
}

export interface ManagerReadTenantDictItemDTO {
    typeId: string;
    page: number;
    pageSize: number;
}

export interface ManagerUpdateTenantDictItemDTO extends BaseManagerUpdateDTO {
    itemValue?: string;
    parentId?: string;
    sortOrder?: number;
    isDefault?: boolean;
    status?: number;
}

export interface ManagerDeleteTenantDictItemDTO extends BaseManagerDeleteDTO {
    ids: string[];
}

class TenantDictItemManagerControllerClass extends BaseManagerController<
    TenantDictItem,
    ManagerCreateTenantDictItemDTO,
    ManagerReadTenantDictItemDTO,
    ManagerUpdateTenantDictItemDTO,
    ManagerDeleteTenantDictItemDTO
> {
    constructor() {
        super('/manager/tenant/dict-item');
    }

    tree(typeId: string) {
        return doGet<TenantDictItemTreeNode[]>(`/api${this.baseUrl}/tree`, { typeId });
    }
}

export const TenantDictItemManagerController = new TenantDictItemManagerControllerClass();
