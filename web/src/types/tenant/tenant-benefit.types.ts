import type {BaseEntity} from "@/types/BaseEntity.ts";

export interface TenantTireBenefitFeature extends BaseEntity {
    featureKey: string;
    name: string;
    description: string | null;
    featureType: number;
    defaultValue: string | null;
}

export interface TenantTireBenefitValue extends BaseEntity {
    tireTypeId: string;
    featureId: string;
    featureValue: string;
}
