package com.lovelycatv.crystalframework.messagechannel.websocket.service

/**
 * Brute-force protection for WebSocket authentication entry points. Mirrors the login rate-limit
 * contract but operates on WebSocket-specific Redis keys and configuration.
 *
 * Consumers must call [checkAllowed] or [checkAllowedIpOnly] before verifying credentials,
 * [recordFailure] when verification fails (and account is known), and [recordSuccess] when it succeeds.
 */
interface WebSocketAuthRateLimitService {
    /**
     * Records the attempt and throws [com.lovelycatv.crystalframework.shared.exception.TooManyRequestsException]
     * if the per-IP / per-account sliding window is exceeded or the account is locked out. Fails
     * open (allows the request) when the limiter is disabled or its infrastructure is unavailable.
     */
    suspend fun checkAllowed(ip: String, account: String)

    /**
     * Records the attempt for per-IP sliding window only (no per-account or exponential-backoff).
     * Use this when the account is unknown (e.g., token missing or unparseable before extracting username).
     *
     * Throws [com.lovelycatv.crystalframework.shared.exception.TooManyRequestsException] if the
     * per-IP sliding window is exceeded.
     */
    suspend fun checkAllowedIpOnly(ip: String)

    /** Increments the consecutive-failure counter for [account], applying exponential-backoff lockout. */
    suspend fun recordFailure(account: String)

    /** Clears the failure counter, lockout and account window for [account] on successful auth. */
    suspend fun recordSuccess(account: String)
}
