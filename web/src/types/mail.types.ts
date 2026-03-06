import type {BaseEntity} from "./BaseEntity.ts";

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
