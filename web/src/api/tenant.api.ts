import {BaseManagerController} from "./BaseManagerController.ts";
import type {Tenant, UserTenantVO} from "../types/tenant.types.ts";
import type {BaseManagerReadDTO, BaseManagerUpdateDTO} from "../types/api.types.ts";
import {doGet} from "@/api/system-request.ts";

export const TenantManagerController = new BaseManagerController<
    Tenant,
    ManagerCreateTenantDTO,
    ManagerReadTenantDTO,
    ManagerUpdateTenantDTO
>('/manager/tenant');

export interface ManagerCreateTenantDTO {
    name: string;
    description: string | null;
    ownerUserId: string;
    tireTypeId: string;
    contactName: string;
    contactEmail: string;
    contactPhone: string;
    address: string;
    settings: string | null;
    subscribedTime: string;
    expiresTime: string;
}

export interface ManagerUpdateTenantDTO extends BaseManagerUpdateDTO {
    name?: string | null;
    description?: string | null;
    status?: number | null;
    tireTypeId?: string | null;
    contactName?: string | null;
    contactEmail?: string | null;
    contactPhone?: string | null;
    address?: string | null;
    settings?: string | null;
    subscribedTime?: string | null;
    expiresTime?: string | null;
}

export interface ManagerReadTenantDTO extends BaseManagerReadDTO {
    status?: number | null;
}

export function getJoinedTenants() {
    return doGet<UserTenantVO[]>("/api/tenant/joined");
}