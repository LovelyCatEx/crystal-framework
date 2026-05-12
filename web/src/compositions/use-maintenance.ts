import {getSystemMaintenanceMode} from "@/api/system-settings.api.ts";
import type {SystemMaintenanceStatusVO} from "@/types/system-settings.types.ts";
import useSWR from "swr";

/**
 * Global maintenance status store.
 *
 * Uses a single SWR key so all consumers share the same cache entry and
 * revalidation lifecycle. `revalidateOnFocus` (default true) ensures the
 * status refreshes when the user switches back to the tab.
 *
 * Returns:
 *  - `maintenanceMode`: whether the system is in maintenance
 *  - `canAccess`: whether the current user is allowed through even during maintenance
 *  - `isLoading`: still fetching (no data yet)
 *  - `error`: request failed
 *  - `mutate`: manually trigger a re-fetch (e.g. after toggling maintenance)
 */
export function useMaintenanceStatus() {
    const {data, isLoading, error, mutate} = useSWR<SystemMaintenanceStatusVO>(
        'systemMaintenanceStatus',
        () => getSystemMaintenanceMode().then((res) => res.data!),
    );

    return {
        maintenanceMode: data?.maintenanceMode ?? undefined,
        canAccess: data?.canAccess ?? undefined,
        isLoading,
        error,
        mutate,
    };
}
