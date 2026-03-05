package com.lovelycatv.crystalframework.user.service.impl

import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.mail.MailService
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.user.service.EmailCodeAuthService
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service
import java.time.Duration
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.text.split

@Service
class EmailCodeAuthServiceImpl(
    private val redisService: RedisService,
    private val mailService: MailService,
) : EmailCodeAuthService {
    override suspend fun checkCachedEmailCode(
        redisKey: String,
        emailCode: String
    ) {
        val existingCode = redisService
            .get<String>(redisKey)
            .awaitFirstOrNull()

        if (existingCode == null) {
            throw BusinessException("invalid email code")
        }

        val (_, correctCode) = existingCode.split(":")

        if (emailCode != correctCode) {
            throw BusinessException("incorrect email code")
        }
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

        val code = (100000..999999).random().toString()

        redisService
            .set(redisKey, "${System.currentTimeMillis()}:$code", Duration.ofMinutes(validMinutes))
            .awaitFirstOrNull()

        action.invoke(code, this.mailService)
    }
}