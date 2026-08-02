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
    FORK = 5,
    JOIN = 6,
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

/**
 * Backend `ConditionOperator` (see crystal-approval/types/ConditionOperator.kt). Persisted
 * inside CONDITION node config as a lowercase string — must stay in strict sync with the
 * `APPROVAL_FIELD_OPERATORS` mapping in `approval-form-schema.types.ts`.
 */
export enum ConditionOperator {
    EQ = 'eq',
    NE = 'ne',
    GT = 'gt',
    GTE = 'gte',
    LT = 'lt',
    LTE = 'lte',
    CONTAINS = 'contains',
    IN = 'in',
}

/**
 * Backend `ConditionLogic`. Values match the `@JsonValue` on the Kotlin enum.
 */
export enum ConditionLogic {
    AND = 'and',
    OR = 'or',
}

/**
 * Approval form field type.
 *
 * String-valued for readability inside persisted JSON schemas (definition.formSchema,
 * node.formSchema) and to stay symmetric with the backend ConditionOperator enum which
 * is also serialized as a lowercase string.
 */
export enum ApprovalFieldType {
    TEXT = 'text',
    TEXTAREA = 'textarea',
    NUMBER = 'number',
    BOOLEAN = 'boolean',
    SELECT = 'select',
    RADIO = 'radio',
    CHECKBOX = 'checkbox',
    DATE = 'date',
    DATETIME = 'datetime',
    DICT = 'dict',
}
