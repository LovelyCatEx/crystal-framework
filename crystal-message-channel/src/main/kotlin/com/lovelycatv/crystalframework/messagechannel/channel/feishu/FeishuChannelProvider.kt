package com.lovelycatv.crystalframework.messagechannel.channel.feishu

import com.lovelycatv.crystalframework.messagechannel.channel.MessageChannelProvider
import com.lovelycatv.crystalframework.messagechannel.constants.ChannelType
import com.lovelycatv.crystalframework.messagechannel.types.content.ChainMessage
import com.lovelycatv.crystalframework.messagechannel.types.options.SendOptions
import com.lovelycatv.crystalframework.messagechannel.types.recipient.MessageRecipient

/**
 * Placeholder. Intentionally NOT annotated with @Component so it is NOT picked up by Spring;
 * sending to FEISHU recipients will fail with NO_PROVIDER until this is implemented.
 *
 * When ready: annotate with @Component, inject [FeishuAtResolver], a feishu API client,
 * and a renderer that converts MessageChain → feishu text/post payload.
 */
class FeishuChannelProvider : MessageChannelProvider {

    override val channelType: ChannelType = ChannelType.FEISHU

    override suspend fun send(
        recipient: MessageRecipient,
        message: ChainMessage,
        options: SendOptions,
    ): Nothing = throw UnsupportedOperationException("Feishu channel is not implemented yet")
}
