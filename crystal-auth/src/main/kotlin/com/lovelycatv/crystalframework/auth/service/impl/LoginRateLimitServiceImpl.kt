package com.lovelycatv.crystalframework.auth.service.impl

import com.lovelycatv.crystalframework.auth.constants.LoginRateLimitConstants
import com.lovelycatv.crystalframework.auth.service.LoginRateLimitService
import com.lovelycatv.crystalframework.shared.api.system.SystemModuleClient
import com.lovelycatv.crystalframework.shared.constants.RedisConstants
import com.lovelycatv.crystalframework.shared.exception.RateLimitContext
import com.lovelycatv.crystalframework.shared.exception.TooManyRequestsException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.types.system.SystemSettings
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class LoginRateLimitServiceImpl(
    private val reactiveRedisService: ReactiveRedisService,
    private val systemModuleClient: SystemModuleClient,
) : LoginRateLimitService {
    private val logger = logger()

    override suspend fun checkAllowed(ip: String, account: String) {
        val config = resolveConfig() ?: return

        val nowMs = System.currentTimeMillis()
        val windowMs = config.windowSeconds.toLong() * 1000L

        val reply = try {
            reactiveRedisService.executeScript(
                script = LoginRateLimitConstants.CHECK_SCRIPT,
                keys = listOf(
                    RedisConstants.getLoginLockUntilKey(account),
                    RedisConstants.getLoginRateLimitIpKey(ip),
                    RedisConstants.getLoginRateLimitAccountKey(account),
                ),
                args = listOf(
                    nowMs.toString(),
                    windowMs.toString(),
                    config.maxAttemptsPerIp.toString(),
                    config.maxAttemptsPerAccount.toString(),
                    UUID.randomUUID().toString(),
                ),
            ).awaitFirstOrNull()
        } catch (e: Exception) {
            // Fail open: never block logins because the limiter infrastructure is unavailable.
            logger.warn("Login rate limit check failed, allowing request", e)
            return
        } ?: return

        val (status, retryAfterSeconds) = parseReply(reply)
        when (status) {
            LoginRateLimitConstants.STATUS_LOCKED -> throw TooManyRequestsException(
                LoginRateLimitConstants.MESSAGE_ACCOUNT_LOCKED,
                RateLimitContext(retryAfterSeconds),
            )
            LoginRateLimitConstants.STATUS_WINDOW_EXCEEDED -> throw TooManyRequestsException(
                LoginRateLimitConstants.MESSAGE_TOO_MANY_ATTEMPTS,
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

    private fun parseReply(reply: String): Pair<Int, Long> {
        val parts = reply.split(":")
        val status = parts.getOrNull(0)?.toIntOrNull() ?: LoginRateLimitConstants.STATUS_ALLOWED
        val retryAfterSeconds = parts.getOrNull(1)?.toLongOrNull() ?: 0L
        return status to retryAfterSeconds
    }
}
