/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.messagechannel.types.recipient

import com.lovelycatv.crystalframework.messagechannel.constants.ChannelType

data class EmailRecipient(
    val email: String,
    val displayName: String? = null,
) : MessageRecipient {
    override val channelType: ChannelType = ChannelType.EMAIL
}
