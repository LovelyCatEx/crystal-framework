import {doGet} from "@/api/system-request.ts";

export interface MetricPoint {
    value: number;
    timestamp: number;
}

export interface MetricQueryResponse {
    metricType: string;
    data: MetricPoint[];
}

export async function queryMetric(
    type: string,
    duration: string = "1m",
) {
    return doGet<MetricQueryResponse>(
        "/api/manager/monitor/system-metrics/" + type,
        {duration},
    );
}
