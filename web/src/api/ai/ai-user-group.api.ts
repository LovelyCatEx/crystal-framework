/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {BaseManagerController} from "@/api/BaseManagerController.ts";
import type {AiUserGroupEntity} from "@/types/ai/ai.types.ts";
import type {BaseManagerReadDTO, BaseManagerUpdateDTO} from "@/types/api.types.ts";

export interface ManagerCreateAiUserGroupDTO {
    name: string;
    key: string;
    description: string | null;
    billingMultiplier: string;
    enabled: boolean;
    isDefault: boolean;
    sort: number;
}

export interface ManagerUpdateAiUserGroupDTO extends BaseManagerUpdateDTO {
    name?: string | null;
    key?: string | null;
    description?: string | null;
    billingMultiplier?: string | null;
    enabled?: boolean | null;
    isDefault?: boolean | null;
    sort?: number | null;
}

export interface ManagerReadAiUserGroupDTO extends BaseManagerReadDTO {
    // All filtering is done via query: GroupNode
}

export const AiUserGroupManagerController = new BaseManagerController<
    AiUserGroupEntity,
    ManagerCreateAiUserGroupDTO,
    ManagerReadAiUserGroupDTO,
    ManagerUpdateAiUserGroupDTO
>('/manager/ai-user-group');
