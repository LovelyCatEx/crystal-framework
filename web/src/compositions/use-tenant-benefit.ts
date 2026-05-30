import {getMyTenantBenefits} from "@/api/tenant/tenant-benefit.api.ts";
import {useSWRState} from "@/compositions/use-swr.ts";

const SWR_KEY = 'myTenantBenefits';

export function useTenantBenefit() {
    const [benefits, , isLoading] = useSWRState<Record<string, string>>(
        SWR_KEY,
        getMyTenantBenefits,
    );

    function getBenefitValue(featureKey: string): string | undefined {
        return benefits?.[featureKey];
    }

    function hasFeature(featureKey: string): boolean {
        return benefits?.[featureKey] === 'true';
    }

    function getBenefitLimit(featureKey: string, defaultLimit: number = 0): number {
        const value = benefits?.[featureKey];
        if (value === undefined || value === null) return defaultLimit;
        const parsed = parseInt(value, 10);
        return Number.isNaN(parsed) ? defaultLimit : parsed;
    }

    return {
        benefits,
        isLoading,
        getBenefitValue,
        hasFeature,
        getBenefitLimit,
    };
}
