import {BaseManagerController} from "./BaseManagerController.ts";
import type {BaseManagerReadDTO} from "../types/api.types.ts";

export interface UserLoginLogEntity {
    id: string;
    userId: string | null;
    username: string | null;
    tenantId: string | null;
    loginMethod: number;
    oauth2Type: number | null;
    oauth2Username: string | null;
    oauth2AccountId: string | null;
    success: boolean;
    errorMessage: string | null;
    remoteIp: string | null;
    userAgent: string | null;
    createdTime: string;
    modifiedTime: string;
}

export interface ManagerReadUserLoginLogDTO extends BaseManagerReadDTO {
    userId?: string;
    username?: string;
    tenantId?: string;
    loginMethod?: number;
    oauth2Type?: number;
    success?: boolean;
    remoteIp?: string;
}

export interface ManagerUpdateUserLoginLogDTO {
    id: string;
}

export interface ManagerDeleteUserLoginLogDTO {
    ids: string[];
}

export interface ManagerCreateUserLoginLogDTO {
    placeholder?: string;
}

export const UserLoginLogManagerController = new BaseManagerController<
    UserLoginLogEntity,
    ManagerCreateUserLoginLogDTO,
    ManagerReadUserLoginLogDTO,
    ManagerUpdateUserLoginLogDTO,
    ManagerDeleteUserLoginLogDTO
>('/manager/user-login-logs');