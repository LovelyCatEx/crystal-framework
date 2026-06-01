package com.lovelycatv.crystalframework.messagechannel.service

import com.lovelycatv.crystalframework.messagechannel.types.content.ChainMessage
import com.lovelycatv.crystalframework.messagechannel.types.options.SendOptions
import com.lovelycatv.crystalframework.messagechannel.types.recipient.MessageRecipient
import com.lovelycatv.crystalframework.messagechannel.types.result.SendResult

/**
 * Entry point for sending messages across channels. Other modules should depend on this
 * interface only, never on a concrete channel provider.
 */
interface MessageChannelService {

    suspend fun send(
        recipient: MessageRecipient,
        message: ChainMessage,
        options: SendOptions = SendOptions.DEFAULT,
    ): SendResult

    suspend fun broadcast(
        recipients: List<MessageRecipient>,
        message: ChainMessage,
        options: SendOptions = SendOptions.DEFAULT,
    ): List<SendResult>
}
