import type {BaseEntity} from "@/types/BaseEntity.ts";

export enum TenantBenefitType {
    BOOLEAN = 0,
    LIMIT = 1,
    ENUM = 2,
}

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

// Overview — backend returns this to frontend, so it's a VO
export interface TenantTireBenefitOverviewItemVO {
    featureId: string;
    featureKey: string;
    name: string;
    description: string | null;
    featureType: number;
    defaultValue: string | null;
    value: string | null;
    valueId: string | null;
    isCustomized: boolean;
}
