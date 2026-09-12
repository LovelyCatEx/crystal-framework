package com.lovelycatv.crystalframework.auth.service.impl

import com.lovelycatv.crystalframework.auth.constants.LoginRateLimitConstants
import com.lovelycatv.crystalframework.auth.service.LoginRateLimitService
import com.lovelycatv.crystalframework.shared.api.system.SystemModuleClient
import com.lovelycatv.crystalframework.shared.constants.RedisConstants
import com.lovelycatv.crystalframework.shared.service.ratelimit.GeneralRateLimitService
import com.lovelycatv.crystalframework.shared.service.ratelimit.RateLimitConfig
import com.lovelycatv.crystalframework.shared.service.ratelimit.RateLimitDimension
import com.lovelycatv.crystalframework.shared.types.system.SystemSettings
import org.springframework.stereotype.Service

@Service
class LoginRateLimitServiceImpl(
    private val generalRateLimitService: GeneralRateLimitService,
    private val systemModuleClient: SystemModuleClient,
) : LoginRateLimitService {

    override suspend fun checkAllowed(ip: String, account: String) {
        val config = buildConfig(ip, account) ?: return
        generalRateLimitService.checkAllowed(config, ip, account)
    }

    override suspend fun recordFailure(account: String) {
        val config = buildConfig("", account) ?: return
        generalRateLimitService.recordFailure(config, account)
    }

    override suspend fun recordSuccess(account: String) {
        val config = buildConfig("", account) ?: return
        generalRateLimitService.recordSuccess(config, account)
    }

    /**
     * Builds [RateLimitConfig] from system settings, or null when rate limiting should be skipped
     * (settings missing or the feature disabled).
     */
    private fun buildConfig(ip: String, account: String): RateLimitConfig? {
        val settings = systemModuleClient.getSystemSettings()?.security?.loginRateLimit ?: return null
        if (!settings.enabled) return null

        return RateLimitConfig(
            keyPrefix = RedisConstants.LOGIN_LOCK_PREFIX,
            slidingWindow = RateLimitConfig.SlidingWindowConfig(
                windowSeconds = settings.windowSeconds,
                dimensions = listOf(
                    RateLimitDimension(
                        redisKey = RedisConstants.getLoginRateLimitIpKey(ip),
                        maxCount = settings.maxAttemptsPerIp
                    ),
                    RateLimitDimension(
                        redisKey = RedisConstants.getLoginRateLimitAccountKey(account),
                        maxCount = settings.maxAttemptsPerAccount
                    ),
                ),
            ),
            exponentialBackoff = RateLimitConfig.ExponentialBackoffConfig(
                threshold = settings.lockThreshold,
                baseSeconds = settings.lockBaseSeconds,
                maxSeconds = settings.lockMaxSeconds,
                windowSeconds = settings.windowSeconds,
            ),
            slidingWindowMessage = LoginRateLimitConstants.MESSAGE_TOO_MANY_ATTEMPTS,
            lockoutMessage = LoginRateLimitConstants.MESSAGE_ACCOUNT_LOCKED,
        )
    }
}
