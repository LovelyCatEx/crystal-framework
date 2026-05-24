package com.lovelycatv.crystalframework.monitor.types

data class MetricQueryResponse(
    val metricType: MetricType,
    val data: List<MetricPoint>,
)
