/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

export interface MetricPoint {
    value: number;
    timestamp: number;
}

export interface MetricQueryVO {
    metricType: string;
    data: MetricPoint[];
}
