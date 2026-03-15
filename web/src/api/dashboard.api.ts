import {doGet} from "@/api/system-request.ts";
import type {BusinessStatsVO, SystemMetricsVO} from "@/types/dashboard.types.ts";

/**
 * Get business statistics
 *
 * @param timeRange 1d, 3d, 5d, 1w, 2w, 1m, 3m, 6m, 1y
 */
export async function getBusinessStats(timeRange: string) {
    return doGet<BusinessStatsVO>("/api/manager/dashboard/business-stats", {timeRange});
}

/**
 * Get system metrics
 */
export async function getSystemMetrics() {
    return doGet<SystemMetricsVO>("/api/manager/dashboard/system-metrics");
}
