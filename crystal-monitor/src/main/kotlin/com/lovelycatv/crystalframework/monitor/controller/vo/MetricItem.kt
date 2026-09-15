/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.monitor.controller.vo

/**
 * Metric item with raw values and usage percentage
 */
data class MetricItem(
    val used: Number,
    val total: Number,
    val usage: Number,
)
