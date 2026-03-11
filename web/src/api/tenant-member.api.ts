import {BaseManagerController} from "./BaseManagerController.ts";
import type {BaseManagerDeleteDTO, BaseManagerUpdateDTO} from "../types/api.types.ts";
import type {TenantMemberVO} from "@/types/tenant-member.types.ts";

export interface ManagerCreateTenantMemberDTO {
    tenantId: string;
    memberUserId: string;
    status: number;
}

export interface ManagerUpdateTenantMemberDTO extends BaseManagerUpdateDTO {
    status?: number;
}

export interface ManagerReadTenantMemberDTO {
    tenantId: string;
    searchKeyword?: string;
    status?: number;
    page: number;
    pageSize: number;
}

export interface ManagerDeleteTenantMemberDTO extends BaseManagerDeleteDTO {
    ids: string[];
}

export const TenantMemberStatus = {
    ACTIVE: 1,
    INACTIVE: 0
} as const;

export const TenantMemberStatusMap: Record<number, { label: string; color: string }> = {
    [TenantMemberStatus.ACTIVE]: { label: 'ACTIVE', color: 'green' },
    [TenantMemberStatus.INACTIVE]: { label: 'INACTIVE', color: 'red' }
};

class TenantMemberManagerControllerClass extends BaseManagerController<
    TenantMemberVO,
    ManagerCreateTenantMemberDTO,
    ManagerReadTenantMemberDTO,
    ManagerUpdateTenantMemberDTO,
    ManagerDeleteTenantMemberDTO
> {
    constructor() {
        super('/manager/tenant/member');
    }
}

export const TenantMemberManagerController = new TenantMemberManagerControllerClass();
