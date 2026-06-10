package com.lovelycatv.crystalframework.messagechannel.types.recipient

import com.lovelycatv.crystalframework.messagechannel.constants.ChannelType

data class EmailRecipient(
    val email: String,
    val displayName: String? = null,
) : MessageRecipient {
    override val channelType: ChannelType = ChannelType.EMAIL
}
