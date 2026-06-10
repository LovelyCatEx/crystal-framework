package com.lovelycatv.crystalframework.messagechannel.channel.lark

data class LarkSendMessageResult(
    val success: Boolean,
    val messageId: String? = null,
    val errorCode: String? = null,
    val errorMessage: String? = null,
)
