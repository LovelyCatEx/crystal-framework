package com.lovelycatv.crystalframework.user.service.impl

import com.lovelycatv.crystalframework.mail.service.MailService
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
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
        validMinutes: Long,
        action: suspend (code: String, mailService: MailService) -> Unit
    ) {
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
}
