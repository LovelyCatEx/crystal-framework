import {useSWRState} from "@/compositions/use-swr.ts";
import {doGet} from "@/api/system-request.ts";
import type {TenantMemberProfileVO} from "@/types/tenant/tenant-member.types.ts";

export const useTenantMemberProfile = (memberId?: string | null) => {
    const [member, , isLoading] = useSWRState<TenantMemberProfileVO>(
        memberId ? `getTenantMemberProfile?memberId=${memberId}` : undefined,
        () => doGet<TenantMemberProfileVO>('/api/me/tenant-profile', { memberId: memberId! })
    );

    return { member, isLoading };
}
