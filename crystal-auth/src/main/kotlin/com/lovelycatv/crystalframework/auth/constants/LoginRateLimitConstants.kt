package com.lovelycatv.crystalframework.auth.constants

/**
 * Centralized keys, messages and the lockout Lua script for the brute-force login rate limiter.
 *
 * The two-layer defense is now split across two collaborators:
 *  - The per-IP and per-account sliding windows are enforced by the shared
 *    [com.lovelycatv.crystalframework.shared.service.ratelimit.SlidingWindowRateLimiter].
 *  - The consecutive-failure lockout is login-specific and stays here: [RECORD_FAILURE_SCRIPT]
 *    increments the failure counter and, once the threshold is crossed, sets an exponential-backoff
 *    lockout. It returns `"<applied>:<lockSeconds>"`.
 */
object LoginRateLimitConstants {
    const val MESSAGE_TOO_MANY_ATTEMPTS = "Too many login attempts, please try again later"
    const val MESSAGE_ACCOUNT_LOCKED = "Account temporarily locked due to repeated failures, please try again later"

    /** Exchange attribute carrying the resolved account key (`username:tenantId`) across filter handlers. */
    const val ATTR_LOGIN_ACCOUNT = "crystal.login.rateLimit.account"

    /** Default tenant segment for account keys that are not tenant-scoped (e.g. system-level OAuth bind). */
    const val DEFAULT_TENANT_SEGMENT = "0"

    /** Builds the account rate-limit key `username:tenantId`. */
    fun buildAccountKey(username: String, tenantSegment: String = DEFAULT_TENANT_SEGMENT) = "$username:$tenantSegment"

    /**
     * KEYS[1]=failCounter, KEYS[2]=lockUntil
     * ARGV[1]=nowMs, ARGV[2]=lockThreshold, ARGV[3]=lockBaseSeconds, ARGV[4]=lockMaxSeconds, ARGV[5]=failTtlSeconds
     */
    val RECORD_FAILURE_SCRIPT = """
        local now = tonumber(ARGV[1])
        local threshold = tonumber(ARGV[2])
        local baseSeconds = tonumber(ARGV[3])
        local maxSeconds = tonumber(ARGV[4])
        local failTtl = tonumber(ARGV[5])

        local fails = redis.call('INCR', KEYS[1])
        redis.call('EXPIRE', KEYS[1], failTtl)

        if fails >= threshold then
            local exponent = fails - threshold
            local lockSeconds = baseSeconds * (2 ^ exponent)
            if lockSeconds > maxSeconds then
                lockSeconds = maxSeconds
            end
            lockSeconds = math.floor(lockSeconds)
            redis.call('SET', KEYS[2], now + lockSeconds * 1000, 'PX', lockSeconds * 1000)
            return '1:' .. lockSeconds
        end

        return '0:0'
    """.trimIndent()
}
