import type {BaseEntity} from "../BaseEntity.ts";
import type {BaseManagerReadDTO} from "../api.types.ts";

export interface UserLoginLogEntity extends BaseEntity {
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
