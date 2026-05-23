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
