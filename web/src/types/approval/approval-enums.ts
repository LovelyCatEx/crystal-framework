/**
 * Approval flow enums
 */

export {ResourceScope} from "@/types/BaseScopedEntity.ts";

/** @deprecated Use ResourceScope instead */
export { ResourceScope as ApprovalFlowScope } from "@/types/BaseScopedEntity.ts";

/** Approval flow definition status */
export enum ApprovalFlowDefinitionStatus {
    DRAFT = 0,
    PUBLISHED = 1,
    DISABLED = 2,
}

/** Approval flow instance status */
export enum ApprovalFlowInstanceStatus {
    IN_PROGRESS = 0,
    APPROVED = 1,
    REJECTED = 2,
    CANCELLED = 3,
}

/** Approval flow node type */
export enum ApprovalFlowNodeType {
    START = 0,
    END = 1,
    APPROVAL = 2,
    CONDITION = 3,
    CC = 4,
}

/** Approval mode (all approvers or any one) */
export enum ApprovalFlowApproveMode {
    AND = 0,
    OR = 1,
}

/** Approver assignment strategy */
export enum ApprovalFlowApproverStrategy {
    SPECIFIED_USER = 0,
    SPECIFIED_ROLE = 1,
    DIRECT_SUPERIOR = 2,
    DEPARTMENT_HEAD = 3,
    INITIATOR_CHOOSE = 4,
}

/** Approval flow record action */
export enum ApprovalFlowRecordAction {
    INITIATE = 0,
    APPROVE = 1,
    REJECT = 2,
    SYSTEM_FORWARD = 3,
}

/** Approval flow task status */
export enum ApprovalFlowTaskStatus {
    PENDING = 0,
    APPROVED = 1,
    REJECTED = 2,
    SKIPPED = 3,
}
