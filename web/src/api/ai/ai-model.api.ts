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
    key: string;
    modelName: string;
    displayName: string;
    description: string | null;
    capabilities: string;
    contextWindowTokens: string;
    maxOutputTokens: string | null;
    inputPricePerMillion: string;
    outputPricePerMillion: string;
    cacheReadPricePerMillion: string | null;
    cacheWritePricePerMillion: string | null;
    currencyId: string;
    requestConfig: string;
    enabled: boolean;
    sort: number;
}

export interface ManagerUpdateAiModelDTO extends BaseManagerUpdateDTO {
    providerId?: string | null;
    key?: string | null;
    modelName?: string | null;
    displayName?: string | null;
    description?: string | null;
    capabilities?: string | null;
    contextWindowTokens?: string | null;
    maxOutputTokens?: string | null;
    inputPricePerMillion?: string | null;
    outputPricePerMillion?: string | null;
    cacheReadPricePerMillion?: string | null;
    cacheWritePricePerMillion?: string | null;
    currencyId?: string | null;
    requestConfig?: string | null;
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
