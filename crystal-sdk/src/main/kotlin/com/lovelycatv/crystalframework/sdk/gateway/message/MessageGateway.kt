package com.lovelycatv.crystalframework.sdk.gateway.message

import com.lovelycatv.crystalframework.messagechannel.types.config.ChannelConfig
import com.lovelycatv.crystalframework.messagechannel.types.content.ChainMessage
import com.lovelycatv.crystalframework.messagechannel.types.options.SendOptions
import com.lovelycatv.crystalframework.messagechannel.types.recipient.MessageRecipient
import com.lovelycatv.crystalframework.messagechannel.types.result.SendResult
import com.lovelycatv.crystalframework.sdk.gateway.Gateway

interface MessageGateway : Gateway {

    suspend fun send(
        config: ChannelConfig,
        recipient: MessageRecipient,
        message: ChainMessage,
        options: SendOptions = SendOptions.DEFAULT,
    ): SendResult

    suspend fun broadcast(
        config: ChannelConfig,
        recipients: List<MessageRecipient>,
        message: ChainMessage,
        options: SendOptions = SendOptions.DEFAULT,
    ): List<SendResult>

    suspend fun sendByChannelId(
        channelId: String,
        recipient: MessageRecipient,
        message: ChainMessage,
        options: SendOptions = SendOptions.DEFAULT,
    ): SendResult

    suspend fun broadcastByChannelId(
        channelId: String,
        recipients: List<MessageRecipient>,
        message: ChainMessage,
        options: SendOptions = SendOptions.DEFAULT,
    ): List<SendResult>
}
