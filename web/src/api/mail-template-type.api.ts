import {BaseManagerController} from "./BaseManagerController.ts";
import type {MailTemplateType} from "../types/mail.types.ts";
import type {BaseManagerReadDTO, BaseManagerUpdateDTO} from "../types/api.types.ts";

export const MailTemplateTypeManagerController = new BaseManagerController<
    MailTemplateType,
    ManagerCreateMailTemplateTypeDTO,
    ManagerReadMailTemplateTypeDTO,
    ManagerUpdateMailTemplateTypeDTO
>('/manager/mail-template-type');

export interface ManagerCreateMailTemplateTypeDTO {
    name: string;
    description: string | null;
    variables: string;
    categoryId: string;
    allowMultiple: boolean;
}

export interface ManagerUpdateMailTemplateTypeDTO extends BaseManagerUpdateDTO {
    name?: string | null;
    description?: string | null;
    variables?: string | null;
    categoryId?: string | null;
    allowMultiple?: boolean | null;
}

export interface ManagerReadMailTemplateTypeDTO extends BaseManagerReadDTO {
}
