/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.messagechannel.types.resolver

import com.lovelycatv.crystalframework.messagechannel.types.chain.AtSegment

/**
 * Channel-side hook that turns an abstract [AtSegment] (carrying only platform user/tenant ids)
 * into a channel-native representation `T`.
 *
 * Bindings (platform user ↔ email / feishu open id) are NOT held by the message-channel module;
 * the implementation must be provided by another module (typically the channel's binding service)
 * and exposed as a Spring bean.
 */
fun interface AtResolver<T> {
    suspend fun resolve(at: AtSegment): T?
}
