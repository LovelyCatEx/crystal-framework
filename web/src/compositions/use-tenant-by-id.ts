/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {useSWRComposition} from "@/compositions/use-swr.ts";
import type {Tenant} from "@/types/tenant/tenant.types.ts";
import {TenantManagerController} from "@/api/tenant/tenant.api.ts";

export const useTenantById = (tenantId?: string | null) => {
    const {data: tenant, isLoading} = useSWRComposition<Tenant | null>(
        tenantId ? ['tenant', tenantId] : undefined,
        () => TenantManagerController.getById(tenantId!),
        // Shared config entity; failures are surfaced per-component as a fallback id.
        () => undefined
    );

    return {tenant, isLoading};
};
