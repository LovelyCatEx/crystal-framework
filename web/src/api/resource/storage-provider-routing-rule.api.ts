import {BaseManagerController} from "../BaseManagerController.ts";
import {doPost} from "@/api/system-request.ts";
import type {RuleDistributionType, StorageProviderRoutingRule} from "@/types/resource/storage-provider-routing-rule.types.ts";
import type {BaseManagerDeleteDTO, BaseManagerReadDTO, BaseManagerUpdateDTO} from "@/types/api.types.ts";

export const StorageProviderRoutingRuleManagerController = new BaseManagerController<
    StorageProviderRoutingRule,
    ManagerCreateStorageProviderRoutingRuleDTO,
    ManagerReadStorageProviderRoutingRuleDTO,
    ManagerUpdateStorageProviderRoutingRuleDTO,
    BaseManagerDeleteDTO
>('/manager/storage-provider-routing-rule');

export interface ManagerCreateStorageProviderRoutingRuleDTO {
    name: string;
    conditionTree: string | null;
    targetProviderIds: string;
    distributionType: RuleDistributionType;
    enabled: boolean;
}

export interface ManagerUpdateStorageProviderRoutingRuleDTO extends BaseManagerUpdateDTO {
    name?: string | null;
    conditionTree?: string | null;
    targetProviderIds?: string | null;
    distributionType?: RuleDistributionType | null;
    enabled?: boolean | null;
}

export interface ManagerReadStorageProviderRoutingRuleDTO extends BaseManagerReadDTO {
    enabled?: boolean | null;
}

export async function reorderStorageProviderRoutingRules(orderedIds: string[]) {
    return doPost("/api/manager/storage-provider-routing-rule/reorder", {orderedIds}, {'Content-Type': 'application/json'});
}
