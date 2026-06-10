package com.lovelycatv.crystalframework.messagechannel.channel.email

import com.lovelycatv.crystalframework.mail.service.MailService
import com.lovelycatv.crystalframework.messagechannel.channel.MessageChannelProvider
import com.lovelycatv.crystalframework.messagechannel.constants.ChannelType
import com.lovelycatv.crystalframework.messagechannel.constants.MessageChannelErrorCodes
import com.lovelycatv.crystalframework.messagechannel.types.config.ChannelConfig
import com.lovelycatv.crystalframework.messagechannel.types.config.EmailChannelConfig
import com.lovelycatv.crystalframework.messagechannel.types.content.ChainMessage
import com.lovelycatv.crystalframework.messagechannel.types.options.SendOptions
import com.lovelycatv.crystalframework.messagechannel.types.recipient.EmailRecipient
import com.lovelycatv.crystalframework.messagechannel.types.recipient.MessageRecipient
import com.lovelycatv.crystalframework.messagechannel.types.result.SendResult
import com.lovelycatv.crystalframework.shared.types.system.SystemSettings
import com.lovelycatv.vertex.log.logger
import org.springframework.stereotype.Component

/**
 * Delegates actual SMTP delivery to [MailService] from crystal-mail.
 *
 * SMTP credentials are supplied per-call via [EmailChannelConfig]. The provider does not
 * read system settings — the caller (or the routing service) is responsible for resolving
 * the config first.
 */
@Component
class EmailChannelProvider(
    private val mailService: MailService,
    private val renderer: ChainToHtmlRenderer,
    emailAtResolvers: List<EmailAtResolver>,
) : MessageChannelProvider {
    private val logger = logger()

    private val emailAtResolver: EmailAtResolver? = emailAtResolvers.firstOrNull()

    override val channelType: ChannelType = ChannelType.EMAIL

    override fun supports(recipient: MessageRecipient): Boolean = recipient is EmailRecipient

    override suspend fun send(
        config: ChannelConfig,
        recipient: MessageRecipient,
        message: ChainMessage,
        options: SendOptions,
    ): SendResult {
        if (recipient !is EmailRecipient) {
            return SendResult.failed(
                channelType = channelType,
                errorCode = MessageChannelErrorCodes.BAD_RECIPIENT,
                errorMessage = "EmailChannelProvider requires EmailRecipient but got ${recipient::class.simpleName}",
            )
        }
        if (config !is EmailChannelConfig) {
            return SendResult.failed(
                channelType = channelType,
                errorCode = MessageChannelErrorCodes.INCOMPATIBLE_CHANNEL,
                errorMessage = "EmailChannelProvider requires EmailChannelConfig but got ${config::class.simpleName}",
            )
        }

        val subject = message.title?.takeIf { it.isNotBlank() } ?: DEFAULT_SUBJECT
        val html = renderer.render(message.chain, emailAtResolver)

        return try {
            mailService.sendMail(
                smtpOverride = config.toSmtp(),
                to = recipient.email,
                subject = subject,
                content = html,
            )
            SendResult.success(channelType)
        } catch (e: Exception) {
            logger.error("EmailChannelProvider failed to send mail to {}: {}", recipient.email, e.message, e)
            SendResult.failed(
                channelType = channelType,
                errorCode = ERR_SMTP_FAILED,
                errorMessage = e.message ?: "smtp send failed",
            )
        }
    }

    private fun EmailChannelConfig.toSmtp(): SystemSettings.Mail.SMTP =
        SystemSettings.Mail.SMTP(
            host = host,
            port = port,
            username = username,
            password = password,
            ssl = ssl,
            fromEmail = fromEmail,
        )

    private companion object {
        const val DEFAULT_SUBJECT = "(no subject)"
        const val ERR_SMTP_FAILED = "SMTP_FAILED"
    }
}
