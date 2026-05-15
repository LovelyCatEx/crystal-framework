package com.lovelycatv.crystalframework.script.lsp

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter

@Configuration
class LspWebSocketConfig(
    private val kotlinLspWebSocketHandler: KotlinLspWebSocketHandler
) {
    @Bean
    fun lspWebSocketHandlerMapping(): HandlerMapping {
        val map = mapOf("/ws/lsp/kotlin" to kotlinLspWebSocketHandler)
        return SimpleUrlHandlerMapping(map, -1)
    }

    @Bean
    fun lspWebSocketHandlerAdapter(): WebSocketHandlerAdapter {
        return WebSocketHandlerAdapter()
    }
}
