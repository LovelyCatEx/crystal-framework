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
