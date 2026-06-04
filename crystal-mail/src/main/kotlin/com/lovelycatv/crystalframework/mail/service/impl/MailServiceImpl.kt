package com.lovelycatv.crystalframework.mail.service.impl

import com.lovelycatv.crystalframework.mail.entity.MailTemplateEntity
import com.lovelycatv.crystalframework.mail.service.MailService
import com.lovelycatv.crystalframework.mail.service.MailTemplateService
import com.lovelycatv.crystalframework.mail.utils.resolveMailTemplatePlaceholders
import com.lovelycatv.crystalframework.shared.api.system.SystemModuleClient
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.types.system.SystemSettings
import com.lovelycatv.vertex.log.logger
import jakarta.annotation.Resource
import jakarta.mail.internet.MimeMessage
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.annotation.Lazy
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import java.util.*

@Service
class MailServiceImpl(
    private val mailTemplateService: MailTemplateService,
    private val systemModuleClient: SystemModuleClient
) : MailService {
    private val logger = logger()

    @Lazy
    @Resource
    private lateinit var self: MailService

    private var mailSender: JavaMailSender? = null

    override fun refreshInstance() {
        this.mailSender = null
    }

    override suspend fun getJavaMailSender(): JavaMailSender {
        if (this.mailSender == null) {
            val settings = systemModuleClient.getSystemSettings()
                ?: throw IllegalStateException("System settings not initialized")
            logger.info("MailSender is creating, settings: ${settings.mail}")
            this.mailSender = createMailSender(settings.mail.smtp)
        }

        return this.mailSender!!
    }

    override suspend fun sendMail(to: String, subject: String, content: String) {
        try {
            val instance = this.getJavaMailSender()

            val from = systemModuleClient.getSystemSettings()?.mail?.smtp?.fromEmail
                ?: throw IllegalStateException("System settings not initialized")

            sendMailInternal(instance, from, to, subject, content)

            logger.info("Mail sent successfully, to: $to, subject: $subject, content: ${content.length} bytes")
        } catch (e: Exception) {
            logger.error("Send email to $to failed, subject: $subject, content: $content", e)
            throw BusinessException("Send email to $to failed")
        }
    }

    override suspend fun sendMail(
        smtpOverride: SystemSettings.Mail.SMTP?,
        to: String,
        subject: String,
        content: String,
    ) {
        if (smtpOverride == null) {
            self.sendMail(to, subject, content)
            return
        }

        try {
            val instance = createMailSender(smtpOverride)
            sendMailInternal(instance, smtpOverride.fromEmail, to, subject, content)
            logger.info(
                "Mail sent successfully (override SMTP), to: $to, subject: $subject, content: ${content.length} bytes"
            )
        } catch (e: Exception) {
            logger.error(
                "Send email to {} via override SMTP {} failed, subject: {}",
                to, smtpOverride.host, subject, e
            )
            throw BusinessException("Send email to $to failed")
        }
    }

    override suspend fun sendMailByType(
        to: String,
        templateTypeName: String,
        placeholders: Map<String, String?>
    ) {
        self.sendMail(
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
        self.sendMail(
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

        val title = template.title.resolveMailTemplatePlaceholders(placeholders)
        val content = template.content.resolveMailTemplatePlaceholders(placeholders)

        self.sendMail(to, title, content)
    }

    private fun sendMailInternal(
        sender: JavaMailSender,
        from: String,
        to: String,
        subject: String,
        content: String,
    ) {
        val message: MimeMessage = sender.createMimeMessage()

        MimeMessageHelper(message, true, "UTF-8").apply {
            setFrom(from)
            setTo(to)
            setSubject(subject)
            setText(content, true)
        }

        sender.send(message)
    }

    fun createMailSender(smtp: SystemSettings.Mail.SMTP): JavaMailSender {
        return JavaMailSenderImpl().apply {
            host = smtp.host
            port = smtp.port
            username = smtp.username
            password = smtp.password
            defaultEncoding = "UTF-8"
            javaMailProperties = Properties().apply {
                val timeout = 5000
                this["mail.transport.protocol"] = "smtp"
                this["mail.smtp.auth"] = "true"
                if (smtp.ssl) {
                    // SMTPS (typically port 465)
                    this["mail.smtp.ssl.enable"] = "true"
                    this["mail.smtp.starttls.enable"] = "false"
                } else {
                    // Plain SMTP with STARTTLS upgrade (typically port 587)
                    this["mail.smtp.ssl.enable"] = "false"
                    this["mail.smtp.starttls.enable"] = "true"
                    this["mail.smtp.starttls.required"] = "true"
                }
                this["mail.smtp.ssl.trust"] = smtp.host
                this["mail.smtp.connectiontimeout"] = "$timeout"
                this["mail.smtp.timeout"] = "$timeout"
                this["mail.smtp.writetimeout"] = "$timeout"
                this["mail.smtp.ssl.connectiontimeout"] = "$timeout"
                this["mail.smtp.ssl.timeout"] = "$timeout"
            }
        }
    }
}
