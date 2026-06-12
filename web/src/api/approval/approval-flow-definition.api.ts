import {BaseManagerController} from "@/api/BaseManagerController.ts";
import type {BaseManagerDeleteDTO, BaseManagerReadScopedDTO, BaseManagerUpdateDTO} from "@/types/api.types.ts";
import type {ApprovalFlowDefinition} from "@/types/approval/approval-flow-definition.types.ts";

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
