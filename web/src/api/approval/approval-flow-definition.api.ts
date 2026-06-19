import {BaseManagerController} from "@/api/BaseManagerController.ts";
import type {BaseManagerDeleteDTO, BaseManagerReadScopedDTO, BaseManagerUpdateDTO} from "@/types/api.types.ts";
import type {
    ApprovalFlowDefinition,
    ApprovalFlowDefinitionDetailsVO
} from "@/types/approval/approval-flow-definition.types.ts";
import {doGet, doPost} from "@/api/system-request.ts";

export interface ManagerCreateApprovalFlowDefinitionDTO {
    scope: number;
    scopeId: string;
    name: string;
    description?: string;
    formSchema?: string;
}

export interface ManagerReadApprovalFlowDefinitionDTO extends BaseManagerReadScopedDTO {
}

export interface ManagerUpdateApprovalFlowDefinitionDTO extends BaseManagerUpdateDTO {
    name?: string;
    description?: string;
    status?: number;
    formSchema?: string;
}

export interface ManagerDeleteApprovalFlowDefinitionDTO extends BaseManagerDeleteDTO {
}

class ApprovalFlowDefinitionManagerControllerClass extends BaseManagerController<
    ApprovalFlowDefinition,
    ManagerCreateApprovalFlowDefinitionDTO,
    ManagerReadApprovalFlowDefinitionDTO,
    ManagerUpdateApprovalFlowDefinitionDTO,
    ManagerDeleteApprovalFlowDefinitionDTO
> {
    constructor() {
        super('/manager/approval-flow-definitions');
    }
}

export const ApprovalFlowDefinitionManagerController = new ApprovalFlowDefinitionManagerControllerClass();

export async function getApprovalFlowDefinitionDetails(definitionId: string) {
    return doGet<ApprovalFlowDefinitionDetailsVO>("/api/manager/approval-flow-definitions/detailsById", { definitionId: definitionId })
}

export interface GraphNodeDTO {
    nodeKey: string;
    type: number;
    name: string;
    config?: string | null;
    formSchema?: string | null;
    positionX: number;
    positionY: number;
}

export interface GraphEdgeDTO {
    sourceNodeKey: string;
    targetNodeKey: string;
}

export interface ManagerUpdateApprovalFlowGraphDTO {
    definitionId: string;
    nodes: GraphNodeDTO[];
    edges: GraphEdgeDTO[];
}

export async function updateApprovalFlowGraph(dto: ManagerUpdateApprovalFlowGraphDTO) {
    return doPost("/api/manager/approval-flow-definitions/updateGraph", dto, { 'Content-Type': 'application/json' })
}
