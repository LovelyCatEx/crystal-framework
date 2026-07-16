import type {ApprovalFlowInstance} from "@/types/approval/approval-flow-instance.types.ts";
import type {ApprovalFlowDefinition} from "@/types/approval/approval-flow-definition.types.ts";
import type {ApprovalFlowNode} from "@/types/approval/approval-flow-node.types.ts";
import type {ApprovalFlowEdge} from "@/types/approval/approval-flow-edge.types.ts";

export interface ApprovalNodeStateVO {
    status: number;
    taskIds: string[];
}

export interface ApprovalFlowRecordVO {
    id: string;
    scope: number;
    scopeId: string;
    instanceId: string;
    nodeId: string;
    operatorId: string;
    action: number;
    comment: string | null;
    createdTime: string;
}

export interface ApprovalFlowInstanceDetailsVO {
    instance: ApprovalFlowInstance;
    definition: ApprovalFlowDefinition;
    nodes: ApprovalFlowNode[];
    edges: ApprovalFlowEdge[];
    nodeStates: Record<string, ApprovalNodeStateVO>;
    records: ApprovalFlowRecordVO[];
}
