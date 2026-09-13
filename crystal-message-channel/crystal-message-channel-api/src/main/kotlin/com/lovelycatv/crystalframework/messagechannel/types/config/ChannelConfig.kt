/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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
