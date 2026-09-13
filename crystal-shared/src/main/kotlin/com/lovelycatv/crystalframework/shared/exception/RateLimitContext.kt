/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.exception

/**
 * Attached to a 429 [ApiResponse.data] when a request is rejected by the login rate limiter.
 *
 * [retryAfterSeconds] is the number of seconds the client should wait before retrying; it is
 * either the remaining sliding-window horizon or the remaining exponential-backoff lockout.
 */
data class RateLimitContext(
    val retryAfterSeconds: Long,
)
