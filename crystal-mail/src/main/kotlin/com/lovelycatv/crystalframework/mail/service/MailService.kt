package com.lovelycatv.crystalframework.mail.service

import com.lovelycatv.crystalframework.mail.entity.MailTemplateEntity
import com.lovelycatv.crystalframework.shared.types.system.SystemSettings
import org.springframework.mail.javamail.JavaMailSender

interface MailService {
    fun refreshInstance()

    suspend fun getJavaMailSender(): JavaMailSender

    /**
     * Send a mail using SMTP credentials from system settings.
     */
    suspend fun sendMail(to: String, subject: String, content: String)

    /**
     * Send a mail using the given [smtpOverride]. When [smtpOverride] is null this falls back to
     * [sendMail] (system settings). When non-null, a one-shot [JavaMailSender] is built from the
     * supplied SMTP block and used for this call only — nothing is cached.
     */
    suspend fun sendMail(smtpOverride: SystemSettings.Mail.SMTP?, to: String, subject: String, content: String)

    suspend fun sendMailByType(to: String, templateTypeName: String, placeholders: Map<String, String?>)

    suspend fun sendMail(to: String, templateName: String, placeholders: Map<String, String?>)

    suspend fun sendMail(to: String, template: MailTemplateEntity, placeholders: Map<String, String?>)
}
