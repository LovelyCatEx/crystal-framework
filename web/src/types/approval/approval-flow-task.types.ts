import type {BaseScopedEntity} from "@/types/BaseScopedEntity.ts";

export interface ApprovalFlowTask extends BaseScopedEntity {
    instanceId: string;
    nodeId: string;
    assigneeId: string;
    status: number;
    comment: string | null;
    formData: string | null;
}
