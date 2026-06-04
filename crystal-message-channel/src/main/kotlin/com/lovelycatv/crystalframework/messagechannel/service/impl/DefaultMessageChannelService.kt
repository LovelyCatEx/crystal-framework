package com.lovelycatv.crystalframework.messagechannel.service.impl

import com.lovelycatv.crystalframework.messagechannel.channel.MessageChannelProvider
import com.lovelycatv.crystalframework.messagechannel.constants.ChannelType
import com.lovelycatv.crystalframework.messagechannel.constants.MessageChannelErrorCodes
import com.lovelycatv.crystalframework.messagechannel.service.MessageChannelService
import com.lovelycatv.crystalframework.messagechannel.types.config.ChannelConfig
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
        config: ChannelConfig,
        recipient: MessageRecipient,
        message: ChainMessage,
        options: SendOptions,
    ): SendResult {
        if (config.channelType != recipient.channelType) {
            return SendResult.failed(
                channelType = recipient.channelType,
                errorCode = MessageChannelErrorCodes.INCOMPATIBLE_CHANNEL,
                errorMessage = "ChannelConfig(${config.channelType}) does not match recipient(${recipient.channelType})",
            )
        }

        val provider = providersByType[recipient.channelType]
            ?: return SendResult.failed(
                channelType = recipient.channelType,
                errorCode = MessageChannelErrorCodes.NO_PROVIDER,
                errorMessage = "No provider registered for channel ${recipient.channelType}",
            )

        if (!provider.supports(recipient)) {
            return SendResult.failed(
                channelType = recipient.channelType,
                errorCode = MessageChannelErrorCodes.UNSUPPORTED_RECIPIENT,
                errorMessage = "Provider ${provider::class.simpleName} does not support recipient $recipient",
            )
        }

        return provider.send(config, recipient, message, options)
    }

    override suspend fun broadcast(
        config: ChannelConfig,
        recipients: List<MessageRecipient>,
        message: ChainMessage,
        options: SendOptions,
    ): List<SendResult> = recipients.map { send(config, it, message, options) }
}
