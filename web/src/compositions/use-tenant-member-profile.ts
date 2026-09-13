/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {useSWRState} from "@/compositions/use-swr.ts";
import {doGet} from "@/api/system-request.ts";
import type {TenantMemberProfileVO} from "@/types/tenant/tenant-member.types.ts";

export const useTenantMemberProfile = (memberId?: string | null) => {
    const [member, , isLoading] = useSWRState<TenantMemberProfileVO>(
        memberId ? `getTenantMemberProfile?memberId=${memberId}` : undefined,
        () => doGet<TenantMemberProfileVO>('/api/tenant/me-profile', { memberId: memberId! })
    );

    return { member, isLoading };
}
