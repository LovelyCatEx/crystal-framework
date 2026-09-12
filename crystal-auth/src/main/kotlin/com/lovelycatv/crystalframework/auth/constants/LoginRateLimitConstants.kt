package com.lovelycatv.crystalframework.auth.constants

/**
 * Centralized keys and messages for the brute-force login rate limiter.
 *
 * The two-layer defense is now split across two collaborators:
 *  - The per-IP and per-account sliding windows are enforced by the shared
 *    [com.lovelycatv.crystalframework.shared.service.ratelimit.SlidingWindowRateLimiter].
 *  - The consecutive-failure lockout is enforced by the shared
 *    [com.lovelycatv.crystalframework.shared.service.ratelimit.ExponentialBackoffLockout].
 */
object LoginRateLimitConstants {
    const val MESSAGE_TOO_MANY_ATTEMPTS = "Too many login attempts, please try again later"
    const val MESSAGE_ACCOUNT_LOCKED = "Account temporarily locked due to repeated failures, please try again later"
    const val MESSAGE_INVALID_CREDENTIALS = "invalid username or password"

    /** BCrypt work factor used when the submitted account does not exist. */
    const val DUMMY_PASSWORD_HASH = "{bcrypt}${'$'}2y${'$'}10${'$'}9nwqeG3SNdahXCl3Q6J2mu7GA3MAv1HnKO1wN14.kUcvFmmXwolyK"

    /** Exchange attribute carrying the resolved account key (`username:tenantId`) across filter handlers. */
    const val ATTR_LOGIN_ACCOUNT = "crystal.login.rateLimit.account"

    /** Default tenant segment for account keys that are not tenant-scoped (e.g. system-level OAuth bind). */
    const val DEFAULT_TENANT_SEGMENT = "0"

    /** Builds the account rate-limit key `username:tenantId`. */
    fun buildAccountKey(username: String, tenantSegment: String = DEFAULT_TENANT_SEGMENT) = "$username:$tenantSegment"
}
