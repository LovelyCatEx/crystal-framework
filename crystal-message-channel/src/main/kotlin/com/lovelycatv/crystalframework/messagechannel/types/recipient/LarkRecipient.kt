package com.lovelycatv.crystalframework.messagechannel.types.recipient

import com.lovelycatv.crystalframework.messagechannel.constants.ChannelType

/**
 * At least one of [openId] / [userId] / [unionId] / [email] / [chatId] should be present.
 * Lark's `receive_id_type` query parameter is derived from which id is provided
 * (precedence: openId > userId > unionId > email > chatId).
 */
data class LarkRecipient(
    val openId: String? = null,
    val userId: String? = null,
    val unionId: String? = null,
    val email: String? = null,
    val chatId: String? = null,
) : MessageRecipient {
    override val channelType: ChannelType = ChannelType.LARK
}
