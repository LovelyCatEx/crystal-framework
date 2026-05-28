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
