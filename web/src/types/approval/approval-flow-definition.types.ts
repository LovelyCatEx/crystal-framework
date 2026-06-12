import type {BaseEntity} from "@/types/BaseEntity.ts";

export interface ApprovalFlowDefinition extends BaseEntity {
    scope: number;
    scopeId: string;
    name: string;
    description: string | null;
    currentVersion: number;
    status: number;
    formSchema: string | null;
}

export enum ResourceScope {
    SYSTEM = 0,
    TENANT = 1,
}

export enum ApprovalFlowDefinitionStatus {
    DRAFT = 0,
    PUBLISHED = 1,
    DISABLED = 2,
}
