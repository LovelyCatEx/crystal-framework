/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.messagechannel.types.config

import com.lovelycatv.crystalframework.messagechannel.constants.ChannelType

data class EmailChannelConfig(
    val host: String,
    val port: Int,
    val username: String,
    @field:SensitiveField
    val password: String,
    val ssl: Boolean,
    val fromEmail: String,
) : ChannelConfig {
    override val channelType: ChannelType get() = ChannelType.EMAIL
}
