/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.user.service

import com.lovelycatv.crystalframework.mail.service.MailService

interface EmailCodeAuthService {
    suspend fun checkCachedEmailCode(
        redisKey: String,
        emailCode: String
    )

    suspend fun withSendEmailCode(
        redisKey: String,
        ip: String,
        email: String,
        validMinutes: Long = 5,
        action: suspend (code: String, mailService: MailService) -> Unit
    )
}