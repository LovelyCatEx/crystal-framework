package com.lovelycatv.crystalframework.shared.service.ratelimit

/**
 * A single sliding-window constraint for [SlidingWindowRateLimiter].
 *
 * Each dimension is an independent window: its own Redis key and its own cap. A request is admitted
 * only when every dimension is still under its [maxCount]; the first dimension that would be exceeded
 * rejects the whole request. Typical dimensions are per-IP, per-account/target and a single global key.
 *
 * [redisKey] must already be fully qualified (prefix + identity) — build it via the relevant
 * `RedisConstants` helper so the key space stays centralized and free of magic strings.
 */
data class RateLimitDimension(
    val redisKey: String,
    val maxCount: Int,
)
