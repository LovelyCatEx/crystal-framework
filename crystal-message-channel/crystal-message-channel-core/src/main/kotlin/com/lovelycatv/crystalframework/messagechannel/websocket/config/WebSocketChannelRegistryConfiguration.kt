package com.lovelycatv.crystalframework.messagechannel.websocket.config

import com.lovelycatv.crystalframework.sdk.websocket.WebSocketChannelRegistry
import com.lovelycatv.crystalframework.sdk.websocket.config.WebSocketChannelConfigurer
import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * WebSocket channel registry configuration
 *
 * Expose WebSocketChannelRegistry as Spring Bean and collect all WebSocketChannelConfigurer implementations for registration.
 */
@Configuration
class WebSocketChannelRegistryConfiguration {

    @Bean
    fun webSocketChannelRegistry(
        configurers: ObjectProvider<WebSocketChannelConfigurer>
    ): WebSocketChannelRegistry {
        return WebSocketChannelRegistry().apply {
            configurers.orderedStream().forEach { it.configure(this) }
        }
    }
}
