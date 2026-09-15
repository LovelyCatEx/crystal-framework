/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import type {BaseEntity} from "../BaseEntity.ts";

export interface MailTemplateCategory extends BaseEntity {
    name: string;
    description: string | null;
}

export interface MailTemplateType extends BaseEntity {
    name: string;
    description: string | null;
    variables: string;
    categoryId: string;
    allowMultiple: boolean;
}

export interface MailTemplate extends BaseEntity {
    typeId: string;
    name: string;
    description: string | null;
    title: string;
    content: string;
    active: boolean;
}
