/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.monitor.controller.vo

/**
 * Garbage collection metrics item
 */
data class GCMetricsItem(
    val avgTime: Long,
    val totalTime: Long,
    val count: Long
)
