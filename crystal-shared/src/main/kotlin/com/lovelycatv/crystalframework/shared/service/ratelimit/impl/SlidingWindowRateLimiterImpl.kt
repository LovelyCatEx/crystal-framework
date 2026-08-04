package com.lovelycatv.crystalframework.shared.service.ratelimit.impl

import com.lovelycatv.crystalframework.shared.constants.RateLimitConstants
import com.lovelycatv.crystalframework.shared.exception.RateLimitContext
import com.lovelycatv.crystalframework.shared.exception.TooManyRequestsException
import com.lovelycatv.crystalframework.shared.service.ratelimit.RateLimitDimension
import com.lovelycatv.crystalframework.shared.service.ratelimit.SlidingWindowRateLimiter
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class SlidingWindowRateLimiterImpl(
    private val reactiveRedisService: ReactiveRedisService,
) : SlidingWindowRateLimiter {
    private val logger = logger()

    override suspend fun checkAllowed(
        windowSeconds: Int,
        dimensions: List<RateLimitDimension>,
        message: String,
    ) {
        if (dimensions.isEmpty()) {
            return
        }

        val nowMs = System.currentTimeMillis()
        val windowMs = windowSeconds.toLong() * 1000L

        // ARGV: nowMs, windowMs, member, then one maxCount per key positionally aligned with KEYS.
        val args = buildList {
            add(nowMs.toString())
            add(windowMs.toString())
            add(UUID.randomUUID().toString())
            dimensions.forEach { add(it.maxCount.toString()) }
        }

        val reply = try {
            reactiveRedisService.executeScript(
                script = RateLimitConstants.CHECK_SCRIPT,
                keys = dimensions.map { it.redisKey },
                args = args,
            ).awaitFirstOrNull()
        } catch (e: Exception) {
            // Fail open: never block the protected feature because the limiter infrastructure is down.
            logger.warn("Sliding-window rate limit check failed, allowing request", e)
            return
        } ?: return

        val (status, retryAfterSeconds) = parseReply(reply)
        if (status == RateLimitConstants.STATUS_WINDOW_EXCEEDED) {
            throw TooManyRequestsException(message, RateLimitContext(retryAfterSeconds))
        }
    }

    private fun parseReply(reply: String): Pair<Int, Long> {
        val parts = reply.split(":")
        val status = parts.getOrNull(0)?.toIntOrNull() ?: RateLimitConstants.STATUS_ALLOWED
        val retryAfterSeconds = parts.getOrNull(1)?.toLongOrNull() ?: 0L
        return status to retryAfterSeconds
    }
}
