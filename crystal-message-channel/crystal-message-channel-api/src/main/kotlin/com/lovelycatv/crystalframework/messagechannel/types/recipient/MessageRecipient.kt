/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.messagechannel.types.recipient

import com.lovelycatv.crystalframework.messagechannel.constants.ChannelType

/**
 * The destination of a message. Carries channel-specific addressing
 * info (email address, feishu open id, etc.) — never platform-internal user ids.
 * Caller is responsible for resolving bindings before constructing a recipient.
 */
sealed interface MessageRecipient {
    val channelType: ChannelType
}
