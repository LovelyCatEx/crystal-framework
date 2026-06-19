import type {BaseScopedEntity} from "@/types/BaseScopedEntity.ts";

export interface ApprovalFlowRecord extends BaseScopedEntity {
    instanceId: string;
    nodeId: string;
    operatorId: string;
    action: number;
    comment: string | null;
}
