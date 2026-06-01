package com.lovelycatv.crystalframework.messagechannel.types.recipient

import com.lovelycatv.crystalframework.messagechannel.constants.ChannelType

/**
 * At least one of [openId] / [userId] / [unionId] should be present.
 * Concrete channel impl picks the most precise one available.
 */
data class FeishuRecipient(
    val openId: String? = null,
    val userId: String? = null,
    val unionId: String? = null,
) : MessageRecipient {
    override val channelType: ChannelType = ChannelType.FEISHU
}
