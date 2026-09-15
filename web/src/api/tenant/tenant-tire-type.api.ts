/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {BaseManagerController} from "../BaseManagerController.ts";
import type {TenantTireType} from "@/types/tenant/tenant.types.ts";
import type {BaseManagerReadDTO, BaseManagerUpdateDTO} from "@/types/api.types.ts";

export const TenantTireTypeManagerController = new BaseManagerController<
    TenantTireType,
    ManagerCreateTenantTireTypeDTO,
    ManagerReadTenantTireTypeDTO,
    ManagerUpdateTenantTireTypeDTO
>('/manager/tenant/tire');

export interface ManagerCreateTenantTireTypeDTO {
    name: string;
    description: string | null;
}

export interface ManagerUpdateTenantTireTypeDTO extends BaseManagerUpdateDTO {
    name?: string | null;
    description?: string | null;
}

export interface ManagerReadTenantTireTypeDTO extends BaseManagerReadDTO {
}
