import type {BaseScopedEntity} from "@/types/BaseScopedEntity.ts";

export interface ApprovalFlowInstance extends BaseScopedEntity {
    definitionId: string;
    definitionVersion: number;
    initiatorId: string;
    status: number;
    formData: string | null;
    /**
     * Per-instance snapshot of the definition's `formSchema`, captured at startFlow time.
     * Consumers should read this instead of `definition.formSchema` so already-running
     * instances keep the exact form shape they were initiated with, independent of later
     * edits to the definition. See design record option C.
     */
    formSchemaSnapshot: string | null;
    latestNodeId: string;
}
