/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.messagechannel.types.config

import com.lovelycatv.crystalframework.messagechannel.constants.ChannelType

data class LarkChannelConfig(
    val appId: String,
    @field:SensitiveField
    val appSecret: String,
    val baseUrl: String,
) : ChannelConfig {
    override val channelType: ChannelType get() = ChannelType.LARK
}
