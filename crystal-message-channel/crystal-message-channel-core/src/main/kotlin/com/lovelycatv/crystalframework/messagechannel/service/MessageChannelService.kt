/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.messagechannel.service

import com.lovelycatv.crystalframework.messagechannel.types.config.ChannelConfig
import com.lovelycatv.crystalframework.messagechannel.types.content.ChainMessage
import com.lovelycatv.crystalframework.messagechannel.types.options.SendOptions
import com.lovelycatv.crystalframework.messagechannel.types.recipient.MessageRecipient
import com.lovelycatv.crystalframework.messagechannel.types.result.SendResult

/**
 * Entry point for sending messages across channels. Other modules should depend on this
 * interface only, never on a concrete channel provider.
 *
 * The service is stateless: every call supplies its own [ChannelConfig]. Callers decide where
 * the config comes from (system settings, tenant-level storage, hard-coded test fixture, ...).
 */
interface MessageChannelService {

    suspend fun send(
        config: ChannelConfig,
        recipient: MessageRecipient,
        message: ChainMessage,
        options: SendOptions = SendOptions.DEFAULT,
    ): SendResult

    suspend fun broadcast(
        config: ChannelConfig,
        recipients: List<MessageRecipient>,
        message: ChainMessage,
        options: SendOptions = SendOptions.DEFAULT,
    ): List<SendResult>
}
