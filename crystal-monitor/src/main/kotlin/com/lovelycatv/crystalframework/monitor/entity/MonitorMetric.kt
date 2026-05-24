package com.lovelycatv.crystalframework.monitor.entity

data class MonitorMetric(
    val value: Double,
    val createdTime: Long = System.currentTimeMillis(),
)
