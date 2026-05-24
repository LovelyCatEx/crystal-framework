package com.lovelycatv.crystalframework.monitor.types

data class MetricAggregationResponse(
    val metricType: MetricType,
    val avg: Double?,
    val max: Double?,
    val min: Double?,
    val startTime: Long,
    val endTime: Long,
)
