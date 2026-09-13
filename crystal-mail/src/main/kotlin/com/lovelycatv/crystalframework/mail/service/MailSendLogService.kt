/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.mail.service

fun interface MailSendLogService {
    suspend fun record(
        fromEmail: String,
        toEmail: String,
        subject: String,
        content: String,
        success: Boolean,
        errorMessage: String?,
        userId: Long?,
        tenantId: Long?
    )
}