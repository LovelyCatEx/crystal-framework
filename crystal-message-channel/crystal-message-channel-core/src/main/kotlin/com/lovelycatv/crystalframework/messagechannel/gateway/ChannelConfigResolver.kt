/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.messagechannel.gateway

import com.lovelycatv.crystalframework.messagechannel.types.config.ChannelConfig

fun interface ChannelConfigResolver {
    suspend fun resolveById(channelId: String): ChannelConfig
}
