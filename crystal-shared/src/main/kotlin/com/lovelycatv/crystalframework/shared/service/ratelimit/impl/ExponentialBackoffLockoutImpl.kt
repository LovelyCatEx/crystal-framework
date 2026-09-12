package com.lovelycatv.crystalframework.shared.service.ratelimit.impl

import com.lovelycatv.crystalframework.shared.constants.RateLimitConstants
import com.lovelycatv.crystalframework.shared.exception.RateLimitContext
import com.lovelycatv.crystalframework.shared.exception.TooManyRequestsException
import com.lovelycatv.crystalframework.shared.service.ratelimit.ExponentialBackoffLockout
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service

@Service
class ExponentialBackoffLockoutImpl(
    private val reactiveRedisService: ReactiveRedisService,
) : ExponentialBackoffLockout {
    private val logger = logger()

    override suspend fun checkLockout(keyPrefix: String, identity: String, message: String) {
        val lockUntilKey = "${keyPrefix}until:$identity"

        val lockUntilMs = try {
            reactiveRedisService
                .get<String>(lockUntilKey)
                .awaitFirstOrNull()
                ?.toLongOrNull()
        } catch (e: Exception) {
            logger.warn("Exponential backoff lockout check failed for $identity, allowing request", e)
            return
        } ?: return

        val nowMs = System.currentTimeMillis()
        if (lockUntilMs > nowMs) {
            val retryAfterSeconds = (lockUntilMs - nowMs + 999L) / 1000L
            throw TooManyRequestsException(message, RateLimitContext(retryAfterSeconds))
        }
    }

    override suspend fun recordFailure(
        keyPrefix: String,
        identity: String,
        threshold: Int,
        baseSeconds: Int,
        maxSeconds: Int,
        windowSeconds: Int,
    ) {
        val failCounterKey = "${keyPrefix}fail:$identity"
        val lockUntilKey = "${keyPrefix}until:$identity"

        val nowMs = System.currentTimeMillis()
        val failTtlSeconds = maxOf(maxSeconds, windowSeconds).toLong()

        try {
            reactiveRedisService.executeScript(
                script = RateLimitConstants.RECORD_FAILURE_SCRIPT,
                keys = listOf(failCounterKey, lockUntilKey),
                args = listOf(
                    nowMs.toString(),
                    threshold.toString(),
                    baseSeconds.toString(),
                    maxSeconds.toString(),
                    failTtlSeconds.toString(),
                ),
            ).awaitFirstOrNull()
        } catch (e: Exception) {
            logger.warn("Exponential backoff failure recording failed for $identity", e)
        }
    }

    override suspend fun clearLockout(keyPrefix: String, identity: String) {
        val failCounterKey = "${keyPrefix}fail:$identity"
        val lockUntilKey = "${keyPrefix}until:$identity"

        try {
            reactiveRedisService.removeKey(failCounterKey, lockUntilKey).awaitFirstOrNull()
        } catch (e: Exception) {
            logger.warn("Exponential backoff lockout clear failed for $identity", e)
        }
    }
}
