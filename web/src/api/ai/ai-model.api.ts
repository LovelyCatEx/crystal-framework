/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {BaseManagerController} from "@/api/BaseManagerController.ts";
import type {AiModelEntity} from "@/types/ai/ai.types.ts";
import type {BaseManagerReadDTO, BaseManagerUpdateDTO} from "@/types/api.types.ts";

export interface ManagerCreateAiModelDTO {
    providerId: string;
    name: string;
    key: string;
    description: string | null;
    capabilities: string;
    inputPricePerMillion: string;
    outputPricePerMillion: string;
    maxTokens: number | null;
    enabled: boolean;
    sort: number;
}

export interface ManagerUpdateAiModelDTO extends BaseManagerUpdateDTO {
    providerId?: string | null;
    name?: string | null;
    key?: string | null;
    description?: string | null;
    capabilities?: string | null;
    inputPricePerMillion?: string | null;
    outputPricePerMillion?: string | null;
    maxTokens?: number | null;
    enabled?: boolean | null;
    sort?: number | null;
}

export interface ManagerReadAiModelDTO extends BaseManagerReadDTO {
    // All filtering is done via query: GroupNode
}

export const AiModelManagerController = new BaseManagerController<
    AiModelEntity,
    ManagerCreateAiModelDTO,
    ManagerReadAiModelDTO,
    ManagerUpdateAiModelDTO
>('/manager/ai-model');
