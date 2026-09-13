/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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
