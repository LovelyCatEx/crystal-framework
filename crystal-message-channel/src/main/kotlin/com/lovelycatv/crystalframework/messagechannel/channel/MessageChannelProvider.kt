package com.lovelycatv.crystalframework.messagechannel.channel

import com.lovelycatv.crystalframework.messagechannel.constants.ChannelType
import com.lovelycatv.crystalframework.messagechannel.types.config.ChannelConfig
import com.lovelycatv.crystalframework.messagechannel.types.content.ChainMessage
import com.lovelycatv.crystalframework.messagechannel.types.options.SendOptions
import com.lovelycatv.crystalframework.messagechannel.types.recipient.MessageRecipient
import com.lovelycatv.crystalframework.messagechannel.types.result.SendResult

/**
 * Channel-specific sender. One bean per supported [ChannelType]; the [com.lovelycatv.crystalframework
 * .messagechannel.service.MessageChannelService] routes to the right provider by
 * [config].channelType.
 *
 * Providers are stateless w.r.t. credentials — the caller (or the routing service) supplies a
 * fully-resolved [ChannelConfig] on every send. The provider is responsible for nothing but
 * turning ([config], [recipient], [message]) into a network call.
 *
 * Providers decide on their own how to handle unsupported [com.lovelycatv.crystalframework
 * .messagechannel.types.chain.MessageSegment] types — there is no module-wide policy.
 */
interface MessageChannelProvider {
    val channelType: ChannelType

    fun supports(recipient: MessageRecipient): Boolean = recipient.channelType == channelType

    suspend fun send(
        config: ChannelConfig,
        recipient: MessageRecipient,
        message: ChainMessage,
        options: SendOptions = SendOptions.DEFAULT,
    ): SendResult
}
