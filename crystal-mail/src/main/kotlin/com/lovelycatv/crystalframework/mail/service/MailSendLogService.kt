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