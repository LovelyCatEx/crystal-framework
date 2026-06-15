import type {BaseScopedEntity} from "@/types/BaseScopedEntity.ts";

export interface ApprovalFlowInstance extends BaseScopedEntity {
    definitionId: string;
    definitionVersion: number;
    initiatorId: string;
    status: number;
    formData: string | null;
    latestNodeId: string;
}
