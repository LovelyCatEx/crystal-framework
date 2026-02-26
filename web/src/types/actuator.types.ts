export interface ActuatorMetricResult {
    name: string;
    availableTags: {
        tag: string;
        values: string[];
    }[];
    baseUnit?: string;
    description: string;
    measurements: {
        statistic: string;
        value: string | number;
    }[];
}