/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {BaseManagerController} from "../BaseManagerController.ts";
import type {MailTemplateCategory} from "@/types/mail/mail.types.ts";
import type {BaseManagerReadDTO, BaseManagerUpdateDTO} from "@/types/api.types.ts";

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
