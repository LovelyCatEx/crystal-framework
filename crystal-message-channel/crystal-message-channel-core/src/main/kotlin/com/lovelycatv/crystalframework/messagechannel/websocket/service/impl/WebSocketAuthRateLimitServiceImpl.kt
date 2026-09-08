package com.lovelycatv.crystalframework.messagechannel.websocket.service.impl

import com.lovelycatv.crystalframework.messagechannel.websocket.service.WebSocketAuthRateLimitService
import com.lovelycatv.crystalframework.shared.api.system.SystemModuleClient
import com.lovelycatv.crystalframework.shared.constants.RedisConstants
import com.lovelycatv.crystalframework.shared.service.ratelimit.GeneralRateLimitService
import com.lovelycatv.crystalframework.shared.service.ratelimit.RateLimitConfig
import com.lovelycatv.crystalframework.shared.service.ratelimit.RateLimitDimension
import com.lovelycatv.crystalframework.shared.types.system.SystemSettings
import org.springframework.stereotype.Service

@Service
class WebSocketAuthRateLimitServiceImpl(
    private val generalRateLimitService: GeneralRateLimitService,
    private val systemModuleClient: SystemModuleClient,
) : WebSocketAuthRateLimitService {

    override suspend fun checkAllowed(ip: String, account: String) {
        val config = buildConfig(ip, account) ?: return
        generalRateLimitService.checkAllowed(config, ip, account)
    }

    override suspend fun checkAllowedIpOnly(ip: String) {
        val config = buildIpOnlyConfig(ip) ?: return
        generalRateLimitService.checkAllowed(config, ip, "")
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
        val settings = systemModuleClient.getSystemSettings()?.security?.webSocketAuthRateLimit ?: return null
        if (!settings.enabled) return null

        return RateLimitConfig(
            keyPrefix = RedisConstants.WS_AUTH_LOCK_PREFIX,
            slidingWindow = RateLimitConfig.SlidingWindowConfig(
                windowSeconds = settings.windowSeconds,
                dimensions = listOf(
                    RateLimitDimension(
                        redisKey = RedisConstants.getWsAuthRateLimitIpKey(ip),
                        maxCount = settings.maxAttemptsPerIp
                    ),
                    RateLimitDimension(
                        redisKey = RedisConstants.getWsAuthRateLimitAccountKey(account),
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
            slidingWindowMessage = MESSAGE_TOO_MANY_ATTEMPTS,
            lockoutMessage = MESSAGE_ACCOUNT_LOCKED,
        )
    }

    /**
     * Builds IP-only [RateLimitConfig] for scenarios where account is unknown (token missing/unparseable).
     * Only enables per-IP sliding window, skips per-account and exponential-backoff.
     */
    private fun buildIpOnlyConfig(ip: String): RateLimitConfig? {
        val settings = systemModuleClient.getSystemSettings()?.security?.webSocketAuthRateLimit ?: return null
        if (!settings.enabled) return null

        return RateLimitConfig(
            keyPrefix = RedisConstants.WS_AUTH_LOCK_PREFIX,
            slidingWindow = RateLimitConfig.SlidingWindowConfig(
                windowSeconds = settings.windowSeconds,
                dimensions = listOf(
                    RateLimitDimension(
                        redisKey = RedisConstants.getWsAuthRateLimitIpKey(ip),
                        maxCount = settings.maxAttemptsPerIp
                    ),
                ),
            ),
            exponentialBackoff = null, // No account, no exponential backoff
            slidingWindowMessage = MESSAGE_TOO_MANY_ATTEMPTS,
        )
    }

    companion object {
        const val MESSAGE_TOO_MANY_ATTEMPTS = "Too many WebSocket authentication attempts, please try again later"
        const val MESSAGE_ACCOUNT_LOCKED = "Account temporarily locked due to repeated WebSocket authentication failures, please try again later"
    }
}
