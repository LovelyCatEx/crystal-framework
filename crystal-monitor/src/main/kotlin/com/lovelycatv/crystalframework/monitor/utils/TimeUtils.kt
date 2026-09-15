/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.monitor.utils

object TimeUtils {
    fun parseDuration(duration: String): Long {
        if (duration.isBlank()) return 3600_000L // default 1h
        val value = duration.dropLast(1).toLongOrNull()
            ?: throw IllegalArgumentException("Invalid duration format: $duration")
        return when (val unit = duration.last()) {
            'm' -> value * 60_000L
            'h' -> value * 3600_000L
            'd' -> value * 86400_000L
            else -> throw IllegalArgumentException("Unknown duration unit: $unit (use m, h, d)")
        }
    }
}
