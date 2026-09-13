/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.user.constants

/**
 * Messages for the email-code sending rate limiter (anti mail-bombing).
 *
 * The sliding windows themselves are enforced by the shared
 * [com.lovelycatv.crystalframework.shared.service.ratelimit.SlidingWindowRateLimiter]; this only
 * carries the human-readable rejection message surfaced on the 429 response.
 */
object EmailCodeRateLimitConstants {
    const val MESSAGE_TOO_MANY_SENDS = "Too many verification code requests, please try again later"
}
