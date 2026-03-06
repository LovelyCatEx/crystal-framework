import {BaseManagerController} from "./BaseManagerController.ts";
import type {MailTemplateCategory} from "../types/mail.types.ts";
import type {BaseManagerReadDTO, BaseManagerUpdateDTO} from "../types/api.types.ts";

export const MailTemplateCategoryManagerController = new BaseManagerController<
    MailTemplateCategory,
    ManagerCreateMailTemplateCategoryDTO,
    ManagerReadMailTemplateCategoryDTO,
    ManagerUpdateMailTemplateCategoryDTO
>('/manager/mail-template-category');

export interface ManagerCreateMailTemplateCategoryDTO {
    name: string;
    description: string | null;
}

export interface ManagerUpdateMailTemplateCategoryDTO extends BaseManagerUpdateDTO {
    name?: string | null;
    description?: string | null;
}

export interface ManagerReadMailTemplateCategoryDTO extends BaseManagerReadDTO {
}
