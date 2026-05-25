import {BaseManagerController} from "../BaseManagerController.ts";
import type {BaseManagerDeleteDTO, BaseManagerReadDTO, BaseManagerUpdateDTO} from "@/types/api.types.ts";

export interface AuditLogEntity {
    id: string;
    userId: string;
    username: string;
    tenantId: string | null;
    action: number;
    resourceType: string;
    resourceIds: string | null;
    requestId: string | null;
    httpMethod: string | null;
    path: string | null;
    remoteIp: string | null;
    userAgent: string | null;
    success: boolean;
    errorMessage: string | null;
    createdTime: string;
    modifiedTime: string;
}

export interface ManagerReadAuditLogDTO extends BaseManagerReadDTO {
    userId?: string;
    username?: string;
    action?: number;
    path?: string;
    remoteIp?: string;
}

export interface ManagerUpdateAuditLogDTO extends BaseManagerUpdateDTO {
    id: string;
}

export interface ManagerDeleteAuditLogDTO extends BaseManagerDeleteDTO {
    ids: string[];
}

export interface ManagerCreateAuditLogDTO {
    placeholder?: string;
}

export const AuditLogManagerController = new BaseManagerController<
    AuditLogEntity,
    ManagerCreateAuditLogDTO,
    ManagerReadAuditLogDTO,
    ManagerUpdateAuditLogDTO,
    ManagerDeleteAuditLogDTO
>('/manager/audit-log');
