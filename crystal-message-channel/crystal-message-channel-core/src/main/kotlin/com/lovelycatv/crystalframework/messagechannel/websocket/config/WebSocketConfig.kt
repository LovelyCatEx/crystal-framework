package com.lovelycatv.crystalframework.messagechannel.websocket.config

import com.lovelycatv.crystalframework.messagechannel.websocket.WebSocketGatewayHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter

/**
 * WebSocket configuration
 *
 * Configure WebSocket unified entry point: /ws/v1
 */
@Configuration
class WebSocketConfig {

    @Bean
    fun webSocketHandlerMapping(gatewayHandler: WebSocketGatewayHandler): HandlerMapping {
        val map = mapOf(
            "/ws/v1" to gatewayHandler,
            "/ws/v1/**" to gatewayHandler
        )
        val handlerMapping = SimpleUrlHandlerMapping()
        handlerMapping.order = Ordered.HIGHEST_PRECEDENCE
        handlerMapping.urlMap = map
        return handlerMapping
    }

    @Bean
    fun handlerAdapter(): WebSocketHandlerAdapter {
        return WebSocketHandlerAdapter()
    }

    @Bean
    @Order(0)
    fun webSocketSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .securityMatcher(PathPatternParserServerWebExchangeMatcher("/ws/**"))
            .csrf { it.disable() }
            .authorizeExchange { it.anyExchange().permitAll() }
            .build()
    }
}
