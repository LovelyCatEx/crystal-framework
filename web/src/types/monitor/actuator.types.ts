/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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