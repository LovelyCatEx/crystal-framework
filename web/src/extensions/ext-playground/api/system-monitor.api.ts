import {doGet} from "@/api/system-request.ts";
import type {MetricQueryResponse} from "../types/system-monitor.types.ts";

export async function queryMetric(
    type: string,
    duration: string = "1m",
) {
    return doGet<MetricQueryResponse>(
        "/api/ext/system-monitor/metrics/" + type,
        {duration},
    );
}
