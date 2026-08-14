import {getSystemMaintenanceMode} from "@/api/system/system-settings.api.ts";
import type {SystemMaintenanceStatusVO} from "@/types/system/system-settings.types.ts";
import useSWR from "swr";

/** SWR key of the shared maintenance status. Exported so writers of the underlying settings
 *  (system settings save, maintenance toggle) can force an immediate revalidation without waiting
 *  for `revalidateOnFocus`. */
export const SWR_KEY_SYSTEM_MAINTENANCE_STATUS = 'systemMaintenanceStatus';

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
        SWR_KEY_SYSTEM_MAINTENANCE_STATUS,
        () => getSystemMaintenanceMode().then((res) => res.data!),
        {revalidateOnFocus: true},
    );

    return {
        maintenanceMode: data?.maintenanceMode ?? undefined,
        canAccess: data?.canAccess ?? undefined,
        isLoading,
        error,
        mutate,
    };
}
