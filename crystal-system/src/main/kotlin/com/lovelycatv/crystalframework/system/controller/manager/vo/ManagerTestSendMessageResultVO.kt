/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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
