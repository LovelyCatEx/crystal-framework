/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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
