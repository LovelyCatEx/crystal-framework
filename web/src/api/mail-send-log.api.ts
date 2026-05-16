import {BaseManagerController} from "./BaseManagerController.ts";
import type {BaseManagerReadDTO} from "../types/api.types.ts";

export interface MailSendLogEntity {
    id: string;
    fromEmail: string;
    toEmail: string;
    subject: string;
    content: string;
    success: boolean;
    errorMessage: string | null;
    userId: string | null;
    tenantId: string | null;
    createdTime: string;
    modifiedTime: string;
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

export const MailSendLogManagerController = new BaseManagerController<
    MailSendLogEntity,
    ManagerCreateMailSendLogDTO,
    ManagerReadMailSendLogDTO,
    ManagerUpdateMailSendLogDTO,
    ManagerDeleteMailSendLogDTO
>('/manager/mail-send-logs');
