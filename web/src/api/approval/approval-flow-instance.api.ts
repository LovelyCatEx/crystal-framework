import {BaseManagerController} from "@/api/BaseManagerController.ts";
import type {
    BaseManagerDeleteDTO,
    BaseManagerReadScopedDTO,
    BaseManagerUpdateDTO
} from "@/types/api.types.ts";
import {doPost} from "@/api/system-request.ts";
import type {ApprovalFlowInstance} from "@/types/approval/approval-flow-instance.types.ts";

export interface ManagerCreateApprovalFlowInstanceDTO {
    scope: number;
    scopeId: string;
    definitionId: string;
    definitionVersion: number;
    initiatorId: string;
    formData?: string;
    latestNodeId: string;
}

export type ManagerReadApprovalFlowInstanceDTO = BaseManagerReadScopedDTO;

export interface ManagerUpdateApprovalFlowInstanceDTO extends BaseManagerUpdateDTO {
    status?: number;
    latestNodeId?: string;
    formData?: string;
}

export type ManagerDeleteApprovalFlowInstanceDTO = BaseManagerDeleteDTO;

export interface StartApprovalFlowDTO {
    definitionId: string;
    formData?: string;
}

class ApprovalFlowInstanceManagerControllerClass extends BaseManagerController<
    ApprovalFlowInstance,
    ManagerCreateApprovalFlowInstanceDTO,
    ManagerReadApprovalFlowInstanceDTO,
    ManagerUpdateApprovalFlowInstanceDTO,
    ManagerDeleteApprovalFlowInstanceDTO
> {
    constructor() {
        super('/manager/approval-flow-instances');
    }
}

export const ApprovalFlowInstanceManagerController = new ApprovalFlowInstanceManagerControllerClass();

export async function startApprovalFlow(dto: StartApprovalFlowDTO) {
    return doPost<ApprovalFlowInstance>(
        '/api/manager/approval-flow-instances/start',
        dto,
        {'Content-Type': 'application/json'},
    );
}

