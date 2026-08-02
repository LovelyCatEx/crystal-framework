package com.lovelycatv.crystalframework.auth.constants

/**
 * Centralized keys, messages and Lua scripts for the brute-force login rate limiter.
 *
 * Two atomic Lua scripts back the two-layer defense:
 *  - [CHECK_SCRIPT] enforces per-IP and per-account sliding windows plus the active lockout, and
 *    records the attempt in one round-trip (no check-then-act race).
 *  - [RECORD_FAILURE_SCRIPT] increments the consecutive-failure counter and, once the threshold is
 *    crossed, sets an exponential-backoff lockout.
 *
 * Both scripts return `"<status>:<retryAfterSeconds>"`. For [CHECK_SCRIPT], status is
 * `1` = allowed, `0` = window exceeded, `-1` = locked out.
 */
object LoginRateLimitConstants {
    /** Reply status for [CHECK_SCRIPT]: request is allowed. */
    const val STATUS_ALLOWED = 1

    /** Reply status for [CHECK_SCRIPT]: a sliding window was exceeded. */
    const val STATUS_WINDOW_EXCEEDED = 0

    /** Reply status for [CHECK_SCRIPT]: the account is currently locked out. */
    const val STATUS_LOCKED = -1

    const val MESSAGE_TOO_MANY_ATTEMPTS = "Too many login attempts, please try again later"
    const val MESSAGE_ACCOUNT_LOCKED = "Account temporarily locked due to repeated failures, please try again later"

    /** Exchange attribute carrying the resolved account key (`username:tenantId`) across filter handlers. */
    const val ATTR_LOGIN_ACCOUNT = "crystal.login.rateLimit.account"

    /** Default tenant segment for account keys that are not tenant-scoped (e.g. system-level OAuth bind). */
    const val DEFAULT_TENANT_SEGMENT = "0"

    /** Builds the account rate-limit key `username:tenantId`. */
    fun buildAccountKey(username: String, tenantSegment: String = DEFAULT_TENANT_SEGMENT) = "$username:$tenantSegment"

    /**
     * KEYS[1]=lockUntil, KEYS[2]=ipWindow, KEYS[3]=accountWindow
     * ARGV[1]=nowMs, ARGV[2]=windowMs, ARGV[3]=maxPerIp, ARGV[4]=maxPerAccount, ARGV[5]=member
     */
    val CHECK_SCRIPT = """
        local now = tonumber(ARGV[1])
        local windowMs = tonumber(ARGV[2])
        local maxPerIp = tonumber(ARGV[3])
        local maxPerAccount = tonumber(ARGV[4])
        local member = ARGV[5]

        local lockUntil = redis.call('GET', KEYS[1])
        if lockUntil then
            local lockUntilMs = tonumber(lockUntil)
            if lockUntilMs > now then
                return '-1:' .. math.ceil((lockUntilMs - now) / 1000)
            end
        end

        local windowStart = now - windowMs
        local windowSeconds = math.ceil(windowMs / 1000)

        redis.call('ZREMRANGEBYSCORE', KEYS[2], 0, windowStart)
        if redis.call('ZCARD', KEYS[2]) >= maxPerIp then
            return '0:' .. windowSeconds
        end

        redis.call('ZREMRANGEBYSCORE', KEYS[3], 0, windowStart)
        if redis.call('ZCARD', KEYS[3]) >= maxPerAccount then
            return '0:' .. windowSeconds
        end

        redis.call('ZADD', KEYS[2], now, member)
        redis.call('PEXPIRE', KEYS[2], windowMs)
        redis.call('ZADD', KEYS[3], now, member)
        redis.call('PEXPIRE', KEYS[3], windowMs)

        return '1:0'
    """.trimIndent()

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
