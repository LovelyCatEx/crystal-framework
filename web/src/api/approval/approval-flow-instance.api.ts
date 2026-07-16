import {BaseManagerController} from "@/api/BaseManagerController.ts";
import type {
    BaseManagerDeleteDTO,
    BaseManagerReadScopedDTO,
    BaseManagerUpdateDTO
} from "@/types/api.types.ts";
import {doGet, doPost} from "@/api/system-request.ts";
import type {ApprovalFlowInstance} from "@/types/approval/approval-flow-instance.types.ts";
import type {ApprovalFlowInstanceDetailsVO} from "@/types/approval/approval-flow-instance-details.types.ts";

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

/**
 * Query approval flow instances initiated by the current user only.
 * Backend force-clears `id` and injects `initiator_id = self`, ignoring any RBAC read-all authority.
 * Use this from personal "my flows" pages so admins do not see everyone's flows via read-all.
 */
export async function queryMyApprovalFlowInstances(dto: ManagerReadApprovalFlowInstanceDTO) {
    return doPost<import('@/types/api.types.ts').PaginatedResponseData<ApprovalFlowInstance>>(
        '/api/manager/approval-flow-instances/my',
        dto,
        {'Content-Type': 'application/json'},
    );
}

/**
 * Fetch the full instance details (definition graph pinned to the instance's definitionVersion,
 * per-node status aggregated from tasks, and every audit record). Backend enforces access:
 * only read-all admins, the initiator, or any user with an assigned task on this instance
 * (assignee) are allowed.
 */
export async function getApprovalFlowInstanceDetails(instanceId: string) {
    return doGet<ApprovalFlowInstanceDetailsVO>(
        '/api/manager/approval-flow-instances/detailsById',
        {instanceId},
    );
}

