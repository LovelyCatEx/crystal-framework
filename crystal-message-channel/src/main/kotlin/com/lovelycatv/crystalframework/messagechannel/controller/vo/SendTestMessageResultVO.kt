package com.lovelycatv.crystalframework.messagechannel.controller.vo

import com.lovelycatv.crystalframework.messagechannel.types.result.SendResult

/**
 * View object for the message-channel test endpoint, mapped from [SendResult].
 *
 * @param channelType resolved channel, value of [com.lovelycatv.crystalframework.messagechannel.constants.ChannelType.typeId].
 */
data class SendTestMessageResultVO(
    val channelType: Int,
    val success: Boolean,
    val errorCode: String? = null,
    val errorMessage: String? = null,
    val providerMessageId: String? = null,
) {
    companion object {
        fun from(result: SendResult): SendTestMessageResultVO = SendTestMessageResultVO(
            channelType = result.channelType.typeId,
            success = result.success,
            errorCode = result.errorCode,
            errorMessage = result.errorMessage,
            providerMessageId = result.providerMessageId,
        )
    }
}
