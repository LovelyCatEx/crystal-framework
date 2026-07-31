import type {BaseEntity} from "../BaseEntity.ts";

export enum RuleDistributionType {
    FIRST_AVAILABLE = 0,
    RANDOM = 1,
}

export interface StorageProviderRoutingRule extends BaseEntity {
    name: string;
    priority: number;
    conditionTree: string | null;
    targetProviderIds: string;
    distributionType: RuleDistributionType;
    enabled: boolean;
}

export interface StorageProviderSimpleVO {
    id: string;
    name: string;
}

export interface EvaluationNodeTraceVO {
    type: 'leaf' | 'group';
    field?: string | null;
    operator?: string | null;
    expectedValue?: string | null;
    actualValue?: string | null;
    logic?: string | null;
    children?: EvaluationNodeTraceVO[] | null;
    matched: boolean;
}

export interface RuleEvaluationTraceVO {
    ruleId: string;
    ruleName: string;
    priority: number;
    matched: boolean;
    conditionTree: EvaluationNodeTraceVO | null;
    selectedProviderIds?: string[] | null;
}

export interface SimulationResultVO {
    ruleTraces: RuleEvaluationTraceVO[];
    finalProvider: StorageProviderSimpleVO | null;
    noRuleMatched: boolean;
}
