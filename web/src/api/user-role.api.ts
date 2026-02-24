import {BaseManagerController} from "./BaseManagerController.ts";
import type {UserRole} from "../types/user-role.types.ts";
import type {BaseManagerReadDTO, BaseManagerUpdateDTO} from "../types/api.types.ts";

export const UserRoleManagerController = new BaseManagerController<
    UserRole,
    ManagerCreateRoleDTO,
    BaseManagerReadDTO,
    ManagerUpdateRoleDTO
>('/manager/user-role');

export interface ManagerCreateRoleDTO {
    name: string;
    description: string | null;
}

export interface ManagerUpdateRoleDTO extends BaseManagerUpdateDTO {
    name?: string | null;
    description?: string | null;
}
