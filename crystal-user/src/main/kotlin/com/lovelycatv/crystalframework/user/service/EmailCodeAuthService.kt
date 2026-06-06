package com.lovelycatv.crystalframework.user.service

import com.lovelycatv.crystalframework.mail.service.MailService

interface EmailCodeAuthService {
    suspend fun checkCachedEmailCode(
        redisKey: String,
        emailCode: String
    )

    suspend fun withSendEmailCode(
        redisKey: String,
        validMinutes: Long = 5,
        action: suspend (code: String, mailService: MailService) -> Unit
    )
}