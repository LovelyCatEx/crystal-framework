package com.lovelycatv.crystalframework.system.controller.vo

/**
 * Metric item with raw values and usage percentage
 */
data class MetricItem(
    val used: Number,
    val total: Number,
    val usage: Number,
)
