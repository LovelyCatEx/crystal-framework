/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {BaseManagerController} from "../../BaseManagerController.ts";
import type {UserRole} from "@/types/user/rbac/user-role.types.ts";
import type {BaseManagerReadDTO, BaseManagerUpdateDTO} from "@/types/api.types.ts";

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
