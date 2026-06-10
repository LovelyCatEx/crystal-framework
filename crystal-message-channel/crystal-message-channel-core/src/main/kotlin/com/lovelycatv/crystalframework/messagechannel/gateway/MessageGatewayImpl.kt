package com.lovelycatv.crystalframework.messagechannel.gateway

import com.lovelycatv.crystalframework.messagechannel.service.MessageChannelService
import com.lovelycatv.crystalframework.messagechannel.types.config.ChannelConfig
import com.lovelycatv.crystalframework.messagechannel.types.content.ChainMessage
import com.lovelycatv.crystalframework.messagechannel.types.options.SendOptions
import com.lovelycatv.crystalframework.messagechannel.types.recipient.MessageRecipient
import com.lovelycatv.crystalframework.messagechannel.types.result.SendResult
import com.lovelycatv.crystalframework.sdk.gateway.message.MessageGateway

class MessageGatewayImpl(
    private val messageChannelService: MessageChannelService,
    private val channelConfigResolver: ChannelConfigResolver?,
) : MessageGateway {

    override suspend fun send(
        config: ChannelConfig,
        recipient: MessageRecipient,
        message: ChainMessage,
        options: SendOptions,
    ): SendResult {
        return messageChannelService.send(config, recipient, message, options)
    }

    override suspend fun broadcast(
        config: ChannelConfig,
        recipients: List<MessageRecipient>,
        message: ChainMessage,
        options: SendOptions,
    ): List<SendResult> {
        return messageChannelService.broadcast(config, recipients, message, options)
    }

    override suspend fun sendByChannelId(
        channelId: String,
        recipient: MessageRecipient,
        message: ChainMessage,
        options: SendOptions,
    ): SendResult {
        val config = requireResolver().resolveById(channelId)
        return messageChannelService.send(config, recipient, message, options)
    }

    override suspend fun broadcastByChannelId(
        channelId: String,
        recipients: List<MessageRecipient>,
        message: ChainMessage,
        options: SendOptions,
    ): List<SendResult> {
        val config = requireResolver().resolveById(channelId)
        return messageChannelService.broadcast(config, recipients, message, options)
    }

    private fun requireResolver(): ChannelConfigResolver {
        return channelConfigResolver
            ?: throw IllegalStateException("ChannelConfigResolver is not available. Provide a ChannelConfigResolver bean to use sendByChannelId/broadcastByChannelId.")
    }
}
