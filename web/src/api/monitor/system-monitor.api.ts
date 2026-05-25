import {doGet} from "@/api/system-request.ts";
import type {MetricQueryVO} from "@/types/monitor/system-monitor.types.ts";

export async function batchQueryMetrics(
    types: string[],
    duration: string = "1m",
) {
    return doGet<Record<string, MetricQueryVO>>(
        "/api/manager/monitor/system-metrics/query/batch",
        {types: types.join(","), duration},
    );
}
