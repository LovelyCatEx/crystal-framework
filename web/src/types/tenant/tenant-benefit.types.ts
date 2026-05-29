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
