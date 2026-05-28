package com.lovelycatv.crystalframework.monitor.controller.vo

/**
 * Metric item with raw values and usage percentage
 */
data class MetricItem(
    val used: Number,
    val total: Number,
    val usage: Number,
)
