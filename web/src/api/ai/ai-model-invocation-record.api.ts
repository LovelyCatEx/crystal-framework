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
