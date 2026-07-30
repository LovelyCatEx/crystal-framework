/**
 * Approval flow node config types. These are the payload shapes persisted inside
 * `ApprovalFlowNodeEntity.config` as JSON — kept in strict sync with the backend
 * Kotlin data classes under `crystal-approval/types/`.
 */

import type {ConditionLogic, ConditionOperator} from "@/types/approval/approval-enums.ts";

/** Approval node config (see backend `ApprovalNodeConfig`). */
export interface ApprovalNodeConfig {
    approveMode: number;
    strategy: number;
    strategyParams: Record<string, unknown>;
}

/** CC (carbon copy) node config (see backend `CcNodeConfig`). */
export interface CcNodeConfig {
    userIds: string[];
    roleIds: string[];
    channelIds: string[];
}

/**
 * A leaf condition — one field compared with one operator against a scalar or list value.
 * Serialization discriminator is `type: "condition"` (see backend `ConditionNode` @JsonSubTypes).
 */
export interface ConditionLeaf {
    type: 'condition';
    field: string;
    operator: ConditionOperator;
    value?: unknown;
    values?: unknown[];
}

/**
 * A group of conditions joined by AND / OR. Serialization discriminator is `type: "group"`.
 */
export interface ConditionGroup {
    type: 'group';
    logic: ConditionLogic;
    children: ConditionNode[];
}

export type ConditionNode = ConditionLeaf | ConditionGroup;

/** One CONDITION route — a target node id (backend Long, so string here) plus its condition tree. */
export interface ConditionRoute {
    /** Backend Long — string because that's the Long serialization contract project-wide. */
    targetNodeId: string;
    condition: ConditionNode;
}

/** CONDITION node config (see backend `ConditionNodeConfig`). */
export interface ConditionNodeConfig {
    routes: ConditionRoute[];
}

/** Convenience union — narrow via node type when needed. */
export type ApprovalFlowNodeConfig = ApprovalNodeConfig | ConditionNodeConfig | CcNodeConfig;
