package com.lovelycatv.crystalframework.messagechannel.service.impl

import com.lovelycatv.crystalframework.messagechannel.channel.MessageChannelProvider
import com.lovelycatv.crystalframework.messagechannel.constants.ChannelType
import com.lovelycatv.crystalframework.messagechannel.service.MessageChannelService
import com.lovelycatv.crystalframework.messagechannel.types.content.ChainMessage
import com.lovelycatv.crystalframework.messagechannel.types.options.SendOptions
import com.lovelycatv.crystalframework.messagechannel.types.recipient.MessageRecipient
import com.lovelycatv.crystalframework.messagechannel.types.result.SendResult
import com.lovelycatv.vertex.log.logger
import org.springframework.stereotype.Service

@Service
class DefaultMessageChannelService(
    providers: List<MessageChannelProvider>,
) : MessageChannelService {
    private val logger = logger()

    private val providersByType: Map<ChannelType, MessageChannelProvider> =
        providers.associateBy { it.channelType }
            .also {
                logger.info(
                    "MessageChannelService initialized with {} provider(s): {}",
                    it.size,
                    it.keys.joinToString(", "),
                )
            }

    override suspend fun send(
        recipient: MessageRecipient,
        message: ChainMessage,
        options: SendOptions,
    ): SendResult {
        val provider = providersByType[recipient.channelType]
            ?: return SendResult.failed(
                channelType = recipient.channelType,
                errorCode = ERR_NO_PROVIDER,
                errorMessage = "No provider registered for channel ${recipient.channelType}",
            )

        if (!provider.supports(recipient)) {
            return SendResult.failed(
                channelType = recipient.channelType,
                errorCode = ERR_UNSUPPORTED_RECIPIENT,
                errorMessage = "Provider ${provider::class.simpleName} does not support recipient $recipient",
            )
        }

        return provider.send(recipient, message, options)
    }

    override suspend fun broadcast(
        recipients: List<MessageRecipient>,
        message: ChainMessage,
        options: SendOptions,
    ): List<SendResult> = recipients.map { send(it, message, options) }

    private companion object {
        const val ERR_NO_PROVIDER = "NO_PROVIDER"
        const val ERR_UNSUPPORTED_RECIPIENT = "UNSUPPORTED_RECIPIENT"
    }
}
