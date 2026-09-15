/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.monitor.controller.vo

/**
 * System resource metrics data VO
 */
data class SystemMetricsVO(
    val cpuUsage: MetricItem,
    val memoryUsage: MetricItem,
    val jvmHeapMemory: MetricItem,
    val jvmNonHeapMemory: MetricItem,
    val dbConnections: MetricItem,
    val systemLoad: MetricItem,
    val diskUsage: MetricItem,
    val gcMetrics: GCMetricsItem,
    val serverInfo: ServerInfo
)
