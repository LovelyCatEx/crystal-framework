import type {BaseEntity} from "./BaseEntity.ts";

export enum PermissionType {
    ACTION = 0,
    MENU = 1,
    COMPONENT = 2
}

export interface UserPermission extends BaseEntity {
    name: string;
    type: number;
    description: string | null;
    path: string | null;
}