package com.lovelycatv.crystalframework.auth.service

/**
 * Brute-force protection for the password-verification entry points (form login and OAuth account
 * binding). All identifiers are the raw client IP and the account key `username:tenantId`.
 *
 * Consumers must call [checkAllowed] before verifying credentials, [recordFailure] when
 * verification fails, and [recordSuccess] when it succeeds.
 */
interface LoginRateLimitService {
    /**
     * Records the attempt and throws [com.lovelycatv.crystalframework.shared.exception.TooManyRequestsException]
     * if the per-IP / per-account sliding window is exceeded or the account is locked out. Fails
     * open (allows the request) when the limiter is disabled or its infrastructure is unavailable.
     */
    suspend fun checkAllowed(ip: String, account: String)

    /** Increments the consecutive-failure counter for [account], applying exponential-backoff lockout. */
    suspend fun recordFailure(account: String)

    /** Clears the failure counter, lockout and account window for [account] on successful auth. */
    suspend fun recordSuccess(account: String)
}
