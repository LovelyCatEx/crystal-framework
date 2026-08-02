package com.lovelycatv.crystalframework.shared.constants

/**
 * Shared Lua script and reply contract for the generic sliding-window rate limiter
 * ([com.lovelycatv.crystalframework.shared.service.ratelimit.SlidingWindowRateLimiter]).
 *
 * [CHECK_SCRIPT] enforces an arbitrary number of per-key sliding windows atomically: it prunes each
 * window, rejects if any window is already at its cap, and only then records the attempt across all
 * windows in a single round-trip (no check-then-act race).
 *
 * The script returns `"<status>:<retryAfterSeconds>"` where status is [STATUS_ALLOWED] (`1`) or
 * [STATUS_WINDOW_EXCEEDED] (`0`); `retryAfterSeconds` is the window horizon when rejected.
 */
object RateLimitConstants {
    /** Reply status for [CHECK_SCRIPT]: request is allowed. */
    const val STATUS_ALLOWED = 1

    /** Reply status for [CHECK_SCRIPT]: one of the sliding windows was exceeded. */
    const val STATUS_WINDOW_EXCEEDED = 0

    /**
     * KEYS = the N window keys. ARGV layout:
     *   ARGV[1] = nowMs
     *   ARGV[2] = windowMs
     *   ARGV[3] = member (unique ZSET member for this attempt)
     *   ARGV[4 .. 3+N] = per-key max count, positionally aligned with KEYS.
     *
     * Two passes so no window is mutated before every window has been checked: first prune + verify
     * all caps, then record into all windows only if every cap passed.
     */
    val CHECK_SCRIPT = """
        local now = tonumber(ARGV[1])
        local windowMs = tonumber(ARGV[2])
        local member = ARGV[3]
        local windowStart = now - windowMs
        local windowSeconds = math.ceil(windowMs / 1000)
        local n = #KEYS

        for i = 1, n do
            local maxCount = tonumber(ARGV[3 + i])
            redis.call('ZREMRANGEBYSCORE', KEYS[i], 0, windowStart)
            if redis.call('ZCARD', KEYS[i]) >= maxCount then
                return '0:' .. windowSeconds
            end
        end

        for i = 1, n do
            redis.call('ZADD', KEYS[i], now, member)
            redis.call('PEXPIRE', KEYS[i], windowMs)
        end

        return '1:0'
    """.trimIndent()
}
