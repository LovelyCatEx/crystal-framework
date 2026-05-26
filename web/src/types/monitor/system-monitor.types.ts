export interface MetricPoint {
    value: number;
    timestamp: number;
}

export interface MetricQueryVO {
    metricType: string;
    data: MetricPoint[];
}
