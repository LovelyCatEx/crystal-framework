package com.lovelycatv.crystalframework.messagechannel.gateway

import com.lovelycatv.crystalframework.messagechannel.types.config.ChannelConfig

fun interface ChannelConfigResolver {
    suspend fun resolveById(channelId: String): ChannelConfig
}
