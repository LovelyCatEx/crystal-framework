import {BaseManagerController} from "@/api/BaseManagerController.ts";
import type {
    BaseManagerDeleteDTO,
    BaseManagerReadScopedDTO,
    BaseManagerUpdateDTO
} from "@/types/api.types.ts";
import {doPost} from "@/api/system-request.ts";
import type {ApprovalFlowTask} from "@/types/approval/approval-flow-task.types.ts";

export interface ManagerCreateApprovalFlowTaskDTO {
    scope: number;
    scopeId: string;
    instanceId: string;
    nodeId: string;
    assigneeId: string;
    formData?: string;
}

export type ManagerReadApprovalFlowTaskDTO = BaseManagerReadScopedDTO;

export interface ManagerUpdateApprovalFlowTaskDTO extends BaseManagerUpdateDTO {
    status?: number;
    comment?: string;
    formData?: string;
}

export type ManagerDeleteApprovalFlowTaskDTO = BaseManagerDeleteDTO;

export interface HandleApprovalFlowTaskDTO {
    taskId: string;
    approved: boolean;
    comment?: string;
    formData?: string;
}

class ApprovalFlowTaskManagerControllerClass extends BaseManagerController<
    ApprovalFlowTask,
    ManagerCreateApprovalFlowTaskDTO,
    ManagerReadApprovalFlowTaskDTO,
    ManagerUpdateApprovalFlowTaskDTO,
    ManagerDeleteApprovalFlowTaskDTO
> {
    constructor() {
        super('/manager/approval-flow-tasks');
    }
}

export const ApprovalFlowTaskManagerController = new ApprovalFlowTaskManagerControllerClass();

export async function handleApprovalFlowTask(dto: HandleApprovalFlowTaskDTO) {
    return doPost(
        '/api/manager/approval-flow-tasks/handle',
        dto,
        {'Content-Type': 'application/json'},
    );
}
