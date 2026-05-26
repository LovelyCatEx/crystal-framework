import type {BaseEntity} from "../BaseEntity.ts";
import type {BaseManagerReadDTO} from "../api.types.ts";

export interface MailSendLogEntity extends BaseEntity {
    fromEmail: string;
    toEmail: string;
    subject: string;
    content: string;
    success: boolean;
    errorMessage: string | null;
    userId: string | null;
    tenantId: string | null;
}

export interface ManagerReadMailSendLogDTO extends BaseManagerReadDTO {
    keyword?: string;
    toEmail?: string;
    success?: boolean;
    userId?: string;
    tenantId?: string;
    startTime?: string;
    endTime?: string;
}

export interface ManagerUpdateMailSendLogDTO {
    id: string;
}

export interface ManagerDeleteMailSendLogDTO {
    ids: string[];
}

export interface ManagerCreateMailSendLogDTO {
    placeholder?: string;
}
