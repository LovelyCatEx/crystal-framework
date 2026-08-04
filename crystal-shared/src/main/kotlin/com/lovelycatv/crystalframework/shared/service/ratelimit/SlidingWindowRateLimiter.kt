package com.lovelycatv.crystalframework.shared.service.ratelimit

/**
 * Generic multi-dimension sliding-window rate limiter backed by Redis ZSETs.
 *
 * Shared by any entry point that needs "at most N events per rolling window" protection — login
 * brute-force defense and email-code sending both build on it. Each call evaluates an arbitrary set
 * of [RateLimitDimension]s atomically (see [com.lovelycatv.crystalframework.shared.constants.RateLimitConstants.CHECK_SCRIPT]).
 *
 * Implementations must fail open: if the limiter infrastructure is unavailable, the request is
 * allowed rather than blocked, so a Redis outage never takes the protected feature offline.
 */
interface SlidingWindowRateLimiter {
    /**
     * Records the attempt across every dimension and throws
     * [com.lovelycatv.crystalframework.shared.exception.TooManyRequestsException] (carrying the retry
     * horizon) if any dimension's window is already at its cap. No-op when [dimensions] is empty.
     *
     * @param windowSeconds the rolling window length shared by all [dimensions].
     * @param message the human-readable message attached to the 429 response when rejected.
     */
    suspend fun checkAllowed(
        windowSeconds: Int,
        dimensions: List<RateLimitDimension>,
        message: String,
    )
}
