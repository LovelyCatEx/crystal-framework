package com.lovelycatv.crystalframework.messagechannel.types.config

import com.lovelycatv.crystalframework.messagechannel.constants.ChannelType

/**
 * Channel-specific credentials/options consumed by [com.lovelycatv.crystalframework
 * .messagechannel.channel.MessageChannelProvider].
 *
 * The message-channel module is stateless: every send call is parameterized by a
 * [ChannelConfig] supplied by the caller. The module does not load configs from
 * SystemSettings or the database itself.
 */
sealed interface ChannelConfig {
    val channelType: ChannelType
}
