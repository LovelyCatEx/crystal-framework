/**
 * Approval flow node config types
 */

/** Condition logic */
export const ConditionLogic = {
    AND: 'AND',
    OR: 'OR',
} as const;

export type ConditionLogicType = typeof ConditionLogic[keyof typeof ConditionLogic];

/** Expression rule for condition evaluation */
export interface ExpressionRule {
    field: string;
    operator: string;
    value: unknown;
}

/** Condition route to target node */
export interface ConditionRoute {
    targetNodeId: string;
    logic: ConditionLogicType;
    rules: ExpressionRule[];
}

/** Node config union type (sealed class) */
export type ApprovalFlowNodeConfig = ApprovalNodeConfig | ConditionNodeConfig | CcNodeConfig;

/** Approval node config */
export interface ApprovalNodeConfig {
    approveMode: number;
    strategy: number;
    strategyParams: Record<string, unknown>;
}

/** CC (carbon copy) node config */
export interface CcNodeConfig {
    userIds: string[];
    roleIds: string[];
    channelIds: string[];
}

/** Condition node config */
export interface ConditionNodeConfig {
    conditions: ConditionRoute[];
}
