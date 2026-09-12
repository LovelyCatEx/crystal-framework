package com.lovelycatv.crystalframework.user.service.impl

import com.lovelycatv.crystalframework.mail.service.MailService
import com.lovelycatv.crystalframework.shared.api.system.SystemModuleClient
import com.lovelycatv.crystalframework.shared.constants.RedisConstants
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.ratelimit.GeneralRateLimitService
import com.lovelycatv.crystalframework.shared.service.ratelimit.RateLimitConfig
import com.lovelycatv.crystalframework.shared.service.ratelimit.RateLimitDimension
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.user.constants.EmailCodeRateLimitConstants
import com.lovelycatv.crystalframework.user.service.EmailCodeAuthService
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.time.Duration

private const val MAX_VERIFY_ATTEMPTS = 5
private const val ATTEMPTS_LOCK_MINUTES = 30L
private const val CODE_MIN_INCLUSIVE = 100000
private const val CODE_MAX_INCLUSIVE = 999999
private const val ATTEMPTS_KEY_SUFFIX = ":attempts"

@Service
class EmailCodeAuthServiceImpl(
    private val redisService: ReactiveRedisService,
    private val mailService: MailService,
    private val generalRateLimitService: GeneralRateLimitService,
    private val systemModuleClient: SystemModuleClient,
) : EmailCodeAuthService {
    private val secureRandom = SecureRandom()

    override suspend fun checkCachedEmailCode(
        redisKey: String,
        emailCode: String
    ) {
        val attemptsKey = "$redisKey$ATTEMPTS_KEY_SUFFIX"

        val attempts = redisService
            .get<String>(attemptsKey)
            .awaitFirstOrNull()
            ?.toIntOrNull() ?: 0
        if (attempts >= MAX_VERIFY_ATTEMPTS) {
            throw BusinessException("too many failed attempts, please request a new email code")
        }

        val existingCode = redisService
            .get<String>(redisKey)
            .awaitFirstOrNull()
            ?: throw BusinessException("invalid email code")

        val (_, correctCode) = existingCode.split(":")

        if (emailCode != correctCode) {
            redisService
                .set(attemptsKey, (attempts + 1).toString(), Duration.ofMinutes(ATTEMPTS_LOCK_MINUTES))
                .awaitFirstOrNull()
            throw BusinessException("incorrect email code")
        }

        // Verified — invalidate both keys to prevent replay/re-verification
        redisService.removeKey(redisKey, attemptsKey).awaitFirstOrNull()
    }

    override suspend fun withSendEmailCode(
        redisKey: String,
        ip: String,
        email: String,
        validMinutes: Long,
        action: suspend (code: String, mailService: MailService) -> Unit
    ) {
        // Anti mail-bombing: per-IP / per-email / global sliding windows. Runs before the per-email
        // 60s cooldown so it also caps sends spread across many distinct target addresses.
        checkSendRateLimit(ip, email)

        val existingCode = redisService
            .get<String>(redisKey)
            .awaitFirstOrNull()

        if (existingCode != null) {
            val codeCreatedTime = existingCode.split(":")[0].toLong()
            if (System.currentTimeMillis() - codeCreatedTime <= 60 * 1000L) {
                throw BusinessException("request email code frequently")
            }
        }

        val code = (secureRandom.nextInt(CODE_MAX_INCLUSIVE - CODE_MIN_INCLUSIVE + 1) + CODE_MIN_INCLUSIVE).toString()

        redisService
            .set(redisKey, "${System.currentTimeMillis()}:$code", Duration.ofMinutes(validMinutes))
            .awaitFirstOrNull()

        // Reset attempts counter on re-issue so the new code gets a fresh window
        redisService.removeKey("$redisKey$ATTEMPTS_KEY_SUFFIX").awaitFirstOrNull()

        action.invoke(code, this.mailService)
    }

    /**
     * Enforces the email-code sending windows via the shared limiter. No-op when the feature is
     * disabled or its settings are unavailable (the limiter itself fails open on Redis errors).
     */
    private suspend fun checkSendRateLimit(ip: String, email: String) {
        val settings = systemModuleClient.getSystemSettings()?.security?.emailCodeRateLimit ?: return
        if (!settings.enabled) {
            return
        }

        val config = RateLimitConfig(
            keyPrefix = "mail:code:lock:", // EmailCode doesn't use exponential backoff, but keyPrefix is required
            slidingWindow = RateLimitConfig.SlidingWindowConfig(
                windowSeconds = settings.windowSeconds,
                dimensions = listOf(
                    RateLimitDimension(RedisConstants.getMailCodeRateLimitIpKey(ip), settings.maxPerIp),
                    RateLimitDimension(RedisConstants.getMailCodeRateLimitEmailKey(email), settings.maxPerEmail),
                    RateLimitDimension(RedisConstants.MAIL_CODE_RATE_LIMIT_GLOBAL_KEY, settings.maxGlobal),
                ),
            ),
            exponentialBackoff = null, // EmailCode only uses sliding window, no exponential backoff
            slidingWindowMessage = EmailCodeRateLimitConstants.MESSAGE_TOO_MANY_SENDS,
        )

        generalRateLimitService.checkAllowed(config, ip, email)
    }
}
