export interface MetricPoint {
    value: number;
    timestamp: number;
}

export interface MetricQueryResponse {
    metricType: string;
    data: MetricPoint[];
}
