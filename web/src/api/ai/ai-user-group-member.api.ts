import {BaseManagerController} from "@/api/BaseManagerController.ts";
import type {BaseManagerReadDTO, BaseManagerUpdateDTO} from "@/types/api.types.ts";

export interface AiUserGroupMemberEntity {
    id: string;
    userGroupId: string;
    userId: string;
    createdTime: string;
    modifiedTime: string;
    deletedTime: string | null;
}

export interface ManagerCreateAiUserGroupMemberDTO {
    userGroupId: string;
    userId: string;
}

export interface ManagerReadAiUserGroupMemberDTO extends BaseManagerReadDTO {
    userGroupId?: string;
    userId?: string;
}

export interface ManagerUpdateAiUserGroupMemberDTO extends BaseManagerUpdateDTO {
    userGroupId?: string;
    userId?: string;
}

export const AiUserGroupMemberManagerController = new BaseManagerController<
    AiUserGroupMemberEntity,
    ManagerCreateAiUserGroupMemberDTO,
    ManagerReadAiUserGroupMemberDTO,
    ManagerUpdateAiUserGroupMemberDTO
>('/manager/ai-user-group-member');
