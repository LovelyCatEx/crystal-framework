package com.lovelycatv.crystalframework.shared.service.ratelimit

/**
 * High-level rate-limit service that composes sliding-window and exponential-backoff layers based on
 * runtime configuration.
 *
 * Use this when you need flexible rate-limit defense:
 * - Only [RateLimitConfig.slidingWindow]: rate limiting (e.g., email-code sending).
 * - Only [RateLimitConfig.exponentialBackoff]: brute-force defense without per-window cap.
 * - Both: maximum protection (e.g., login, WebSocket auth).
 *
 * The service delegates to [SlidingWindowRateLimiter] and [ExponentialBackoffLockout] based on which
 * layers are configured, providing a unified interface for all rate-limit scenarios.
 */
interface GeneralRateLimitService {
    /**
     * Checks all configured rate-limit layers and throws
     * [com.lovelycatv.crystalframework.shared.exception.TooManyRequestsException] if any layer rejects.
     *
     * Fails open: if a layer's infrastructure is unavailable, that layer is skipped and the request
     * proceeds to the next layer or is allowed.
     *
     * @param config Rate-limit configuration specifying which layers to apply.
     * @param ip Client IP address (used by sliding-window per-IP dimension if configured).
     * @param account Account identifier (used by sliding-window per-account dimension and exponential-backoff if configured).
     */
    suspend fun checkAllowed(config: RateLimitConfig, ip: String, account: String)

    /**
     * Records a failure for the exponential-backoff layer (if configured). No-op if
     * [RateLimitConfig.exponentialBackoff] is null.
     *
     * @param config Rate-limit configuration.
     * @param account Account identifier that failed authentication.
     */
    suspend fun recordFailure(config: RateLimitConfig, account: String)

    /**
     * Clears failure counters and lockout markers for the exponential-backoff layer, and clears
     * the sliding-window per-account counter (if configured). Typically called on successful
     * authentication.
     *
     * @param config Rate-limit configuration.
     * @param account Account identifier that succeeded.
     */
    suspend fun recordSuccess(config: RateLimitConfig, account: String)
}
