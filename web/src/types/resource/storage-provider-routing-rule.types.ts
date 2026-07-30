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
