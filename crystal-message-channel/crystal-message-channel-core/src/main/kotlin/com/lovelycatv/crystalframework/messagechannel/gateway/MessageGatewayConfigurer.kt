/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.messagechannel.gateway

import com.lovelycatv.crystalframework.messagechannel.service.MessageChannelService
import com.lovelycatv.crystalframework.sdk.gateway.GatewayRegistry
import com.lovelycatv.crystalframework.sdk.gateway.config.GatewayConfigurer
import com.lovelycatv.crystalframework.sdk.gateway.message.MessageGateway
import org.springframework.beans.factory.ObjectProvider
import org.springframework.stereotype.Component

@Component
class MessageGatewayConfigurer(
    private val messageChannelService: MessageChannelService,
    private val channelConfigResolver: ObjectProvider<ChannelConfigResolver>,
) : GatewayConfigurer {
    override fun configure(registry: GatewayRegistry) {
        registry.register(
            MessageGateway::class,
            MessageGatewayImpl(messageChannelService, channelConfigResolver.ifAvailable)
        )
    }
}
