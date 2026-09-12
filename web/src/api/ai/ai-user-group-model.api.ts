import {BaseManagerController} from "@/api/BaseManagerController.ts";
import type {BaseManagerReadDTO, BaseManagerUpdateDTO} from "@/types/api.types.ts";

export interface AiUserGroupModelEntity {
    id: string;
    userGroupId: string;
    modelId: string;
    createdTime: string;
    modifiedTime: string;
    deletedTime: string | null;
}

export interface ManagerCreateAiUserGroupModelDTO {
    userGroupId: string;
    modelId: string;
}

export interface ManagerReadAiUserGroupModelDTO extends BaseManagerReadDTO {
    userGroupId?: string;
    modelId?: string;
}

export interface ManagerUpdateAiUserGroupModelDTO extends BaseManagerUpdateDTO {
    userGroupId?: string;
    modelId?: string;
}

export const AiUserGroupModelManagerController = new BaseManagerController<
    AiUserGroupModelEntity,
    ManagerCreateAiUserGroupModelDTO,
    ManagerReadAiUserGroupModelDTO,
    ManagerUpdateAiUserGroupModelDTO
>('/manager/ai-user-group-model');
