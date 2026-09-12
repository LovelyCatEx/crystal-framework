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
