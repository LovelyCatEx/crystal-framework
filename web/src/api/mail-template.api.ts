import {BaseManagerController} from "./BaseManagerController.ts";
import type {MailTemplate} from "../types/mail.types.ts";
import type {BaseManagerReadDTO, BaseManagerUpdateDTO} from "../types/api.types.ts";

export const MailTemplateManagerController = new BaseManagerController<
    MailTemplate,
    ManagerCreateMailTemplateDTO,
    ManagerReadMailTemplateDTO,
    ManagerUpdateMailTemplateDTO
>('/manager/mail-template');

export interface ManagerCreateMailTemplateDTO {
    typeId: string;
    name: string;
    description: string | null;
    title: string;
    content: string;
    active: boolean;
}

export interface ManagerUpdateMailTemplateDTO extends BaseManagerUpdateDTO {
    typeId?: string | null;
    name?: string | null;
    description?: string | null;
    title?: string | null;
    content?: string | null;
    active?: boolean | null;
}

export interface ManagerReadMailTemplateDTO extends BaseManagerReadDTO {
}
