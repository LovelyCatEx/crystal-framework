import {BaseManagerController} from "../BaseManagerController.ts";
import type {BaseManagerReadDTO, BaseManagerUpdateDTO} from "@/types/api.types.ts";
import type {TenantTireBenefitFeature, TenantTireBenefitValue} from "@/types/tenant/tenant-benefit.types.ts";
import {doGet} from "../system-request.ts";

// Feature Catalog
export const TenantTireBenefitFeatureManagerController = new BaseManagerController<
    TenantTireBenefitFeature,
    ManagerCreateTenantTireBenefitFeatureDTO,
    ManagerReadTenantTireBenefitFeatureDTO,
    ManagerUpdateTenantTireBenefitFeatureDTO
>('/manager/tenant/tire/benefit/feature');

export interface ManagerCreateTenantTireBenefitFeatureDTO {
    featureKey: string;
    name: string;
    description: string | null;
    featureType: number;
    defaultValue: string | null;
}

export interface ManagerReadTenantTireBenefitFeatureDTO extends BaseManagerReadDTO {
}

export interface ManagerUpdateTenantTireBenefitFeatureDTO extends BaseManagerUpdateDTO {
    featureKey?: string | null;
    name?: string | null;
    description?: string | null;
    featureType?: number | null;
    defaultValue?: string | null;
}

// Tier -> Feature Values
export const TenantTireBenefitValueManagerController = new BaseManagerController<
    TenantTireBenefitValue,
    ManagerCreateTenantTireBenefitValueDTO,
    ManagerReadTenantTireBenefitValueDTO,
    ManagerUpdateTenantTireBenefitValueDTO
>('/manager/tenant/tire/benefit/value');

export interface ManagerCreateTenantTireBenefitValueDTO {
    tireTypeId: string;
    featureId: string;
    featureValue: string;
}

export interface ManagerReadTenantTireBenefitValueDTO extends BaseManagerReadDTO {
}

export interface ManagerUpdateTenantTireBenefitValueDTO extends BaseManagerUpdateDTO {
    tireTypeId?: string | null;
    featureId?: string | null;
    featureValue?: string | null;
}

// Current tenant benefits (runtime query)
export function getMyTenantBenefits() {
    return doGet<Record<string, string>>('/api/tenant/benefits');
}
