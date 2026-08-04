package com.lovelycatv.crystalframework.shared.exception

/**
 * Thrown when a request is rejected by a rate limiter (e.g. brute-force login protection).
 *
 * Maps to a 429 [ApiResponse] carrying an optional [RateLimitContext] so the frontend can render
 * a retry hint.
 */
class TooManyRequestsException(
    message: String,
    val context: RateLimitContext? = null,
) : RuntimeException(message)
