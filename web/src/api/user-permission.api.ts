import {BaseManagerController} from "./BaseManagerController.ts";
import type {UserPermission} from "../types/user-permission.types.ts";
import type {BaseManagerReadDTO, BaseManagerUpdateDTO} from "../types/api.types.ts";

export const UserPermissionManagerController = new BaseManagerController<
    UserPermission,
    ManagerCreatePermissionDTO,
    BaseManagerReadDTO,
    ManagerUpdatePermissionDTO
>('/manager/user-permission');

export interface ManagerCreatePermissionDTO {
    name: string;
    description: string | null;
    type: number;
    path: string | null;
}

export interface ManagerUpdatePermissionDTO extends BaseManagerUpdateDTO {
    name?: string | null;
    description?: string | null;
    type?: number | null;
    path?: string | null;
}