import {BaseManagerController} from "../BaseManagerController.ts";
import type {BaseManagerDeleteDTO, BaseManagerUpdateDTO, PaginatedResponseData} from "@/types/api.types.ts";
import type {TenantMateVO, TenantMemberVO} from "@/types/tenant/tenant-member.types.ts";
import {doPost, type ApiResponse} from "../system-request.ts";

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

export interface QueryTenantMatesDTO {
    tenantId: string;
    page?: number;
    pageSize?: number;
}

export async function queryTenantMates(
    dto: QueryTenantMatesDTO,
): Promise<ApiResponse<PaginatedResponseData<TenantMateVO>>> {
    return doPost('/api/tenant/member/query-mates', dto, {'Content-Type': 'application/json'});
}

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
