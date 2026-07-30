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
    /**
     * Instance-level schema snapshot (see backend VO doc). NOT the current definition schema —
     * an old instance retains the schema it was initiated with even if the definition has
     * been edited since (design record option C).
     */
    formSchemaSnapshot: string | null;
    nodeFormSchema: string | null;
    instanceFormData: string | null;
}
