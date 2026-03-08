package com.lovelycatv.crystalframework.mail.service.impl

import com.lovelycatv.crystalframework.mail.entity.MailTemplateEntity
import com.lovelycatv.crystalframework.mail.service.MailService
import com.lovelycatv.crystalframework.mail.service.MailTemplateService
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.system.service.SystemSettingsService
import com.lovelycatv.crystalframework.system.types.SystemSettings
import com.lovelycatv.vertex.log.logger
import jakarta.mail.internet.MimeMessage
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import java.util.*

@Service
class MailServiceImpl(
    private val systemSettingsService: SystemSettingsService,
    private val mailTemplateService: MailTemplateService
) : MailService {
    private val logger = logger()

    private var mailSender: JavaMailSender? = null

    override fun refreshInstance() {
        this.mailSender = null
    }

    override suspend fun getJavaMailSender(): JavaMailSender {
        if (this.mailSender == null) {
            val settings = systemSettingsService.getSystemSettings()
            logger.info("MailSender is creating, settings: ${settings.mail}")
            this.mailSender = createMailSender(settings.mail)
        }

        return this.mailSender!!
    }

    override suspend fun sendMail(to: String, subject: String, content: String) {
        try {
            val instance = this.getJavaMailSender()

            val from = systemSettingsService.getSystemSettings().mail.smtp.fromEmail

            val message: MimeMessage = instance.createMimeMessage()

            MimeMessageHelper(message, true, "UTF-8").apply {
                setFrom(from)
                setTo(to)
                setSubject(subject)
                setText(content, true)
            }

            instance.send(message)

            logger.info("Mail sent successfully, to: $to, subject: $subject, content: ${content.length} bytes")
        } catch (e: Exception) {
            logger.error("Send email to $to failed, subject: $subject, content: $content", e)
            throw BusinessException("Send email to $to failed, message: ${e.message}")
        }
    }

    override suspend fun sendMailByType(
        to: String,
        templateTypeName: String,
        placeholders: Map<String, String?>
    ) {
        this.sendMail(
            to,
            mailTemplateService.getAvailableTemplateByTypeName(templateTypeName),
            placeholders
        )
    }

    override suspend fun sendMail(
        to: String,
        templateName: String,
        placeholders: Map<String, String?>
    ) {
        this.sendMail(
            to,
            mailTemplateService
                .getRepository()
                .findByName(templateName)
                .awaitFirstOrNull()
                ?: throw BusinessException("Mail template named $templateName not found"),
            placeholders
        )
    }

    override suspend fun sendMail(
        to: String,
        template: MailTemplateEntity,
        placeholders: Map<String, String?>
    ) {
        if (!template.active) {
            throw BusinessException("This mail template is inactive")
        }

        logger.info("Sending mail to $to by using template: ${template.name}")

        val title = resolvePlaceholders(template.title, placeholders)
        val content = resolvePlaceholders(template.content, placeholders)

        this.sendMail(to, title, content)
    }

    private fun resolvePlaceholders(originalContent: String, placeholders: Map<String, String?>): String {
        var r = originalContent
        placeholders.forEach { (k, v) ->
            r = r.replace("{{$k}}", v ?: "null")
        }
        return r
    }

    fun createMailSender(mailSettings: SystemSettings.Mail): JavaMailSender {
        return JavaMailSenderImpl().apply {
            host = mailSettings.smtp.host
            port = mailSettings.smtp.port
            username = mailSettings.smtp.username
            password = mailSettings.smtp.password
            defaultEncoding = "UTF_8"
            javaMailProperties = Properties().apply {
                val timeout = 5000
                this["mail.transport.protocol"] = "smtp"
                this["mail.smtp.auth"] = "true"
                this["mail.smtp.ssl.enable"] = mailSettings.smtp.ssl.toString()
                this["mail.smtp.starttls.required"] = "true"
                this["mail.smtp.ssl.trust"] = "*"
                this["mail.smtp.connectiontimeout"] = "$timeout"
                this["mail.smtp.timeout"] = "$timeout"
                this["mail.smtp.writetimeout"] = "$timeout"
                this["mail.smtp.ssl.connectiontimeout"] = "$timeout"
                this["mail.smtp.ssl.timeout"] = "$timeout"
            }
        }
    }
}