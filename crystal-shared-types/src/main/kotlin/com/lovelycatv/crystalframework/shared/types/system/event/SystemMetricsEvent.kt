/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.types.system.event

data class SystemMetricsEvent(
    val timestamp: Long,
    val cpuUsage: Double,
    val systemLoadAverage: Double,
    val availableProcessors: Int,
    val memoryTotal: Long,
    val memoryUsed: Long,
    val jvmHeapMax: Long,
    val jvmHeapUsed: Long,
    val jvmNonHeapCommitted: Long,
    val jvmNonHeapUsed: Long,
    val diskTotal: Long,
    val diskUsed: Long,
    val dbActiveConnections: Int,
    val dbMaxConnections: Int,
    val gcCount: Long,
    val gcTotalTime: Long,
)
