/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.messagechannel.websocket.config

import com.lovelycatv.crystalframework.sdk.websocket.WebSocketChannelRegistry
import com.lovelycatv.crystalframework.sdk.websocket.config.WebSocketChannelConfigurer
import com.lovelycatv.crystalframework.sdk.websocket.types.WebSocketChannelHandlerDeclaration
import org.springframework.stereotype.Component

/**
 * Built-in WebSocket channel declaration configurer
 *
 * Register framework's built-in WebSocket channel declarations.
 */
@Component
class WebSocketChannelBuiltinConfigurer : WebSocketChannelConfigurer {

    override fun configure(registry: WebSocketChannelRegistry) {
        registry.registers(listOf(
            WebSocketChannelHandlerDeclaration(
                channelName = "crystal-framework",
                requiresAuth = true,
                description = "Crystal Framework authenticated channel for real-time messaging and notifications"
            ),
            WebSocketChannelHandlerDeclaration(
                channelName = "crystal-framework-pub",
                requiresAuth = false,
                description = "Crystal Framework public channel for anonymous real-time updates and broadcasts"
            ),
        ))
    }
}
