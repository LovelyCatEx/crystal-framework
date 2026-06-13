import type {BaseEntity} from "@/types/BaseEntity.ts";

export interface ApprovalFlowNode extends BaseEntity {
    definitionId: string;
    definitionVersion: number;
    nodeKey: string;
    type: number;
    name: string;
    config: string | null;
    formSchema: string | null;
    positionX: number;
    positionY: number;
}
