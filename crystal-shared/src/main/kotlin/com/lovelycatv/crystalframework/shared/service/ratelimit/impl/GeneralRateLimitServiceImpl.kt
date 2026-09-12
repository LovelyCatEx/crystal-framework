package com.lovelycatv.crystalframework.shared.service.ratelimit.impl

import com.lovelycatv.crystalframework.shared.service.ratelimit.ExponentialBackoffLockout
import com.lovelycatv.crystalframework.shared.service.ratelimit.GeneralRateLimitService
import com.lovelycatv.crystalframework.shared.service.ratelimit.RateLimitConfig
import com.lovelycatv.crystalframework.shared.service.ratelimit.SlidingWindowRateLimiter
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service

@Service
class GeneralRateLimitServiceImpl(
    private val slidingWindowRateLimiter: SlidingWindowRateLimiter,
    private val exponentialBackoffLockout: ExponentialBackoffLockout,
    private val reactiveRedisService: ReactiveRedisService,
) : GeneralRateLimitService {
    private val logger = logger()

    override suspend fun checkAllowed(config: RateLimitConfig, ip: String, account: String) {
        // Layer 1: Exponential-backoff lockout (if configured)
        config.exponentialBackoff?.let { backoffConfig ->
            exponentialBackoffLockout.checkLockout(
                keyPrefix = config.keyPrefix,
                identity = account,
                message = config.lockoutMessage,
            )
        }

        // Layer 2: Sliding-window rate limiting (if configured)
        config.slidingWindow?.let { windowConfig ->
            slidingWindowRateLimiter.checkAllowed(
                windowSeconds = windowConfig.windowSeconds,
                dimensions = windowConfig.dimensions,
                message = config.slidingWindowMessage,
            )
        }
    }

    override suspend fun recordFailure(config: RateLimitConfig, account: String) {
        config.exponentialBackoff?.let { backoffConfig ->
            exponentialBackoffLockout.recordFailure(
                keyPrefix = config.keyPrefix,
                identity = account,
                threshold = backoffConfig.threshold,
                baseSeconds = backoffConfig.baseSeconds,
                maxSeconds = backoffConfig.maxSeconds,
                windowSeconds = backoffConfig.windowSeconds,
            )
        }
    }

    override suspend fun recordSuccess(config: RateLimitConfig, account: String) {
        // Clear exponential backoff lockout (if configured)
        config.exponentialBackoff?.let {
            exponentialBackoffLockout.clearLockout(
                keyPrefix = config.keyPrefix,
                identity = account,
            )
        }

        // Clear sliding-window per-account counter (if configured)
        config.slidingWindow?.let { windowConfig ->
            val accountKeys = windowConfig.dimensions
                .mapNotNull { dim -> dim.redisKey.takeIf { it.contains(account) } }

            if (accountKeys.isNotEmpty()) {
                try {
                    reactiveRedisService.removeKey(*accountKeys.toTypedArray()).awaitFirstOrNull()
                } catch (e: Exception) {
                    logger.warn("Failed to clear sliding-window account counters for $account", e)
                }
            }
        }
    }
}
