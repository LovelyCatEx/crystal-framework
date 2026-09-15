/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.monitor.types

data class MetricQueryResponse(
    val metricType: MetricType,
    val data: List<MetricPoint>,
)
