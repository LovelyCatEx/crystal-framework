import type {BaseScopedEntity} from "@/types/BaseScopedEntity.ts";

export interface ApprovalFlowTask extends BaseScopedEntity {
    instanceId: string;
    nodeId: string;
    assigneeId: string;
    status: number;
    comment: string | null;
    formData: string | null;
}

export interface ApprovalFlowTaskFormViewVO {
    taskId: string;
    instanceId: string;
    nodeId: string;
    nodeType: number;
    definitionId: string;
    definitionFormSchema: string | null;
    nodeFormSchema: string | null;
    instanceFormData: string | null;
}
