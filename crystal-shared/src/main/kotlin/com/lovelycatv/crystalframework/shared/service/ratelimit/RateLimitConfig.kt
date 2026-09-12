package com.lovelycatv.crystalframework.shared.service.ratelimit

/**
 * Configuration for [GeneralRateLimitService], specifying which layers of rate-limit defense to apply.
 *
 * Use [slidingWindow] for rate limiting (at most N attempts per rolling window).
 * Use [exponentialBackoff] for brute-force defense (escalating lockout on consecutive failures).
 * Combine both for maximum protection (e.g., login, WebSocket auth).
 */
data class RateLimitConfig(
    /**
     * Sliding-window rate limiting configuration. Null to disable this layer.
     */
    val slidingWindow: SlidingWindowConfig? = null,

    /**
     * Exponential-backoff lockout configuration. Null to disable this layer.
     */
    val exponentialBackoff: ExponentialBackoffConfig? = null,

    /**
     * Redis key prefix for this rate-limit dimension (e.g., "auth:lock:", "ws:auth:lock:").
     * Used by [exponentialBackoff] to namespace failure counters and lockout markers.
     */
    val keyPrefix: String,

    /**
     * Human-readable message for the 429 response when sliding window is exceeded.
     */
    val slidingWindowMessage: String = "Too many requests, please try again later",

    /**
     * Human-readable message for the 429 response when account is locked out.
     */
    val lockoutMessage: String = "Account temporarily locked due to repeated failures, please try again later",
) {
    /**
     * Sliding-window rate limiting configuration.
     *
     * @param windowSeconds Rolling window length in seconds.
     * @param dimensions List of rate-limit dimensions (per-IP, per-account, global, etc.).
     *                   Each dimension has its own Redis key and cap.
     */
    data class SlidingWindowConfig(
        val windowSeconds: Int,
        val dimensions: List<RateLimitDimension>,
    )

    /**
     * Exponential-backoff lockout configuration.
     *
     * @param threshold Consecutive failures before lockout starts.
     * @param baseSeconds First lockout duration (doubles on each subsequent failure).
     * @param maxSeconds Maximum lockout duration (caps escalation).
     * @param windowSeconds Failure counter TTL (should be >= maxSeconds to retain escalation state).
     */
    data class ExponentialBackoffConfig(
        val threshold: Int,
        val baseSeconds: Int,
        val maxSeconds: Int,
        val windowSeconds: Int,
    )
}
