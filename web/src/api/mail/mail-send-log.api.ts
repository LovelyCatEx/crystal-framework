import {BaseManagerController} from "../BaseManagerController.ts";
import type {
    MailSendLogEntity,
    ManagerCreateMailSendLogDTO,
    ManagerReadMailSendLogDTO,
    ManagerUpdateMailSendLogDTO,
    ManagerDeleteMailSendLogDTO
} from "@/types/mail/mail-send-log.types.ts";

export const MailSendLogManagerController = new BaseManagerController<
    MailSendLogEntity,
    ManagerCreateMailSendLogDTO,
    ManagerReadMailSendLogDTO,
    ManagerUpdateMailSendLogDTO,
    ManagerDeleteMailSendLogDTO
>('/manager/mail-send-logs');
