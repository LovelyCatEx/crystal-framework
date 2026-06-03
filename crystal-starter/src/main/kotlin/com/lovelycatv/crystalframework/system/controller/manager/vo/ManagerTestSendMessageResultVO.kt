package com.lovelycatv.crystalframework.system.controller.manager.vo

import com.lovelycatv.crystalframework.messagechannel.types.result.SendResult

/**
 * View object for the message-channel test endpoint, mapped from [SendResult].
 *
 * @param channelType resolved channel, value of [com.lovelycatv.crystalframework.messagechannel.constants.ChannelType.typeId].
 */
data class ManagerTestSendMessageResultVO(
    val channelType: Int,
    val success: Boolean,
    val errorCode: String? = null,
    val errorMessage: String? = null,
    val providerMessageId: String? = null,
) {
    companion object {
        fun from(result: SendResult): ManagerTestSendMessageResultVO = ManagerTestSendMessageResultVO(
            channelType = result.channelType.typeId,
            success = result.success,
            errorCode = result.errorCode,
            errorMessage = result.errorMessage,
            providerMessageId = result.providerMessageId,
        )
    }
}
