/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {BaseManagerController} from "@/api/BaseManagerController.ts";
import type {AiModelInvocationRecordEntity} from "@/types/ai/ai.types.ts";
import type {BaseManagerReadDTO, BaseManagerUpdateDTO} from "@/types/api.types.ts";

export interface ManagerCreateAiModelInvocationRecordDTO {
    placeholder?: string;
}

export interface ManagerReadAiModelInvocationRecordDTO extends BaseManagerReadDTO {
    // All filtering is done via query: GroupNode
}

export interface ManagerUpdateAiModelInvocationRecordDTO extends BaseManagerUpdateDTO {
    id: string;
}

export const AiModelInvocationRecordManagerController = new BaseManagerController<
    AiModelInvocationRecordEntity,
    ManagerCreateAiModelInvocationRecordDTO,
    ManagerReadAiModelInvocationRecordDTO,
    ManagerUpdateAiModelInvocationRecordDTO
>('/manager/ai-model-invocation-record');
