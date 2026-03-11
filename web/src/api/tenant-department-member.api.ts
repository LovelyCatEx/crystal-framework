import {BaseManagerController} from "./BaseManagerController.ts";
import type {BaseManagerDeleteDTO, BaseManagerUpdateDTO} from "../types/api.types.ts";
import type {TenantDepartmentMemberVO} from "@/types/tenant-department-member.types.ts";

export interface ManagerCreateTenantDepartmentMemberDTO {
    departmentId: string;
    memberId: string;
    roleType?: number;
}

export interface ManagerUpdateTenantDepartmentMemberDTO extends BaseManagerUpdateDTO {
    departmentId: string;
    memberId: string;
    roleType: number;
}

export interface ManagerReadTenantDepartmentMemberDTO {
    departmentId: string;
    memberId?: string;
    roleType?: number;
    searchKeyword?: string;
    page: number;
    pageSize: number;
}

export interface ManagerDeleteTenantDepartmentMemberDTO extends BaseManagerDeleteDTO {
    ids: string[];
}

export const DepartmentMemberRoleType = {
    MEMBER: 0,
    ADMIN: 1,
    SUPER_ADMIN: 2
} as const;

export const DepartmentMemberRoleTypeMap: Record<number, { label: string; color: string }> = {
    [DepartmentMemberRoleType.MEMBER]: { label: 'MEMBER', color: 'default' },
    [DepartmentMemberRoleType.ADMIN]: { label: 'ADMIN', color: 'blue' },
    [DepartmentMemberRoleType.SUPER_ADMIN]: { label: 'SUPER_ADMIN', color: 'purple' }
};

class TenantDepartmentMemberManagerControllerClass extends BaseManagerController<
    TenantDepartmentMemberVO,
    ManagerCreateTenantDepartmentMemberDTO,
    ManagerReadTenantDepartmentMemberDTO,
    ManagerUpdateTenantDepartmentMemberDTO,
    ManagerDeleteTenantDepartmentMemberDTO
> {
    constructor() {
        super('/manager/tenant/department/member');
    }
}

export const TenantDepartmentMemberManagerController = new TenantDepartmentMemberManagerControllerClass();
