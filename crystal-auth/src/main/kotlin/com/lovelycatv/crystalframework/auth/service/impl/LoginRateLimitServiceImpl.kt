package com.lovelycatv.crystalframework.auth.service.impl

import com.lovelycatv.crystalframework.auth.constants.LoginRateLimitConstants
import com.lovelycatv.crystalframework.auth.service.LoginRateLimitService
import com.lovelycatv.crystalframework.shared.api.system.SystemModuleClient
import com.lovelycatv.crystalframework.shared.constants.RedisConstants
import com.lovelycatv.crystalframework.shared.exception.RateLimitContext
import com.lovelycatv.crystalframework.shared.exception.TooManyRequestsException
import com.lovelycatv.crystalframework.shared.service.ratelimit.RateLimitDimension
import com.lovelycatv.crystalframework.shared.service.ratelimit.SlidingWindowRateLimiter
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.types.system.SystemSettings
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service

@Service
class LoginRateLimitServiceImpl(
    private val reactiveRedisService: ReactiveRedisService,
    private val slidingWindowRateLimiter: SlidingWindowRateLimiter,
    private val systemModuleClient: SystemModuleClient,
) : LoginRateLimitService {
    private val logger = logger()

    override suspend fun checkAllowed(ip: String, account: String) {
        val config = resolveConfig() ?: return

        // Layer 1: exponential-backoff lockout (login-specific, driven by consecutive failures).
        checkLockout(account)

        // Layer 2: per-IP and per-account sliding windows via the shared limiter.
        slidingWindowRateLimiter.checkAllowed(
            windowSeconds = config.windowSeconds,
            dimensions = listOf(
                RateLimitDimension(RedisConstants.getLoginRateLimitIpKey(ip), config.maxAttemptsPerIp),
                RateLimitDimension(RedisConstants.getLoginRateLimitAccountKey(account), config.maxAttemptsPerAccount),
            ),
            message = LoginRateLimitConstants.MESSAGE_TOO_MANY_ATTEMPTS,
        )
    }

    /**
     * Rejects the request while an active exponential-backoff lockout is in effect for [account].
     * Fails open when the lockout marker cannot be read.
     */
    private suspend fun checkLockout(account: String) {
        val lockUntilMs = try {
            reactiveRedisService
                .get<String>(RedisConstants.getLoginLockUntilKey(account))
                .awaitFirstOrNull()
                ?.toLongOrNull()
        } catch (e: Exception) {
            logger.warn("Login lockout check failed, allowing request", e)
            return
        } ?: return

        val nowMs = System.currentTimeMillis()
        if (lockUntilMs > nowMs) {
            val retryAfterSeconds = (lockUntilMs - nowMs + 999L) / 1000L
            throw TooManyRequestsException(
                LoginRateLimitConstants.MESSAGE_ACCOUNT_LOCKED,
                RateLimitContext(retryAfterSeconds),
            )
        }
    }

    override suspend fun recordFailure(account: String) {
        val config = resolveConfig() ?: return

        val nowMs = System.currentTimeMillis()
        // Retain the failure counter at least as long as the maximum lockout so escalation is not lost.
        val failTtlSeconds = maxOf(config.lockMaxSeconds, config.windowSeconds).toLong()

        try {
            reactiveRedisService.executeScript(
                script = LoginRateLimitConstants.RECORD_FAILURE_SCRIPT,
                keys = listOf(
                    RedisConstants.getLoginLockFailureKey(account),
                    RedisConstants.getLoginLockUntilKey(account),
                ),
                args = listOf(
                    nowMs.toString(),
                    config.lockThreshold.toString(),
                    config.lockBaseSeconds.toString(),
                    config.lockMaxSeconds.toString(),
                    failTtlSeconds.toString(),
                ),
            ).awaitFirstOrNull()
        } catch (e: Exception) {
            logger.warn("Login rate limit failure recording failed", e)
        }
    }

    override suspend fun recordSuccess(account: String) {
        try {
            reactiveRedisService.removeKey(
                RedisConstants.getLoginLockFailureKey(account),
                RedisConstants.getLoginLockUntilKey(account),
                RedisConstants.getLoginRateLimitAccountKey(account),
            ).awaitFirstOrNull()
        } catch (e: Exception) {
            logger.warn("Login rate limit success reset failed", e)
        }
    }

    /**
     * Returns the effective config, or null when rate limiting should be skipped (settings missing
     * or the feature disabled).
     */
    private fun resolveConfig(): SystemSettings.Security.LoginRateLimit? {
        val loginRateLimit = systemModuleClient.getSystemSettings()?.security?.loginRateLimit ?: return null
        return if (loginRateLimit.enabled) loginRateLimit else null
    }
}
