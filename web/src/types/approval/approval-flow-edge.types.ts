import type {BaseEntity} from "@/types/BaseEntity.ts";

export interface ApprovalFlowEdge extends BaseEntity {
    definitionId: string;
    definitionVersion: number;
    sourceNodeId: string;
    targetNodeId: string;
}
