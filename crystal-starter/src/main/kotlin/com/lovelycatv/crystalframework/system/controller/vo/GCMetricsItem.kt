package com.lovelycatv.crystalframework.system.controller.vo

/**
 * Garbage collection metrics item
 */
data class GCMetricsItem(
    val avgTime: Long,
    val totalTime: Long,
    val count: Long
)
