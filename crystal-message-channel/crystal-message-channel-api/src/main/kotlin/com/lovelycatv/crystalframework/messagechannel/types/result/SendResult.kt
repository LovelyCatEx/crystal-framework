/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.messagechannel.types.result

import com.lovelycatv.crystalframework.messagechannel.constants.ChannelType

data class SendResult(
    val channelType: ChannelType,
    val success: Boolean,
    val errorCode: String? = null,
    val errorMessage: String? = null,
    val providerMessageId: String? = null,
) {
    companion object {
        fun success(channelType: ChannelType, providerMessageId: String? = null): SendResult =
            SendResult(channelType = channelType, success = true, providerMessageId = providerMessageId)

        fun failed(channelType: ChannelType, errorCode: String, errorMessage: String): SendResult =
            SendResult(
                channelType = channelType,
                success = false,
                errorCode = errorCode,
                errorMessage = errorMessage,
            )
    }
}
