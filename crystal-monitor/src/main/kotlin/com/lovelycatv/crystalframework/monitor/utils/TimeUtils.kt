package com.lovelycatv.crystalframework.monitor.utils

object TimeUtils {
    fun parseDuration(duration: String): Long {
        if (duration.isBlank()) return 3600_000L // default 1h
        val value = duration.dropLast(1).toLongOrNull()
            ?: throw IllegalArgumentException("Invalid duration format: $duration")
        val unit = duration.last()
        return when (unit) {
            'm' -> value * 60_000L
            'h' -> value * 3600_000L
            'd' -> value * 86400_000L
            else -> throw IllegalArgumentException("Unknown duration unit: $unit (use m, h, d)")
        }
    }
}
