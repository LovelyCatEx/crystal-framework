package com.lovelycatv.crystalframework.sdk.websocket

import com.lovelycatv.crystalframework.sdk.websocket.types.WebSocketChannelHandlerDeclaration

/**
 * WebSocket channel handler registry
 *
 * Collects channel declarations from multiple Configurers and provides unified query interface.
 * Detects conflicts during registration and immediately throws exception when duplicate channelName is found (fail-fast).
 */
class WebSocketChannelRegistry {
    private val handlers = linkedMapOf<String, WebSocketChannelHandlerDeclaration>()

    /**
     * Register a single channel declaration
     *
     * @throws IllegalStateException if channelName already exists
     */
    fun register(declaration: WebSocketChannelHandlerDeclaration) {
        val channelName = declaration.channelName.trim()
        if (channelName.isBlank()) return

        if (handlers.putIfAbsent(channelName, declaration.copy(channelName = channelName)) != null) {
            throw IllegalStateException("WebSocketChannelRegistry: duplicate channelName '$channelName'")
        }
    }

    /**
     * Register multiple channel declarations
     */
    fun registers(declarations: Iterable<WebSocketChannelHandlerDeclaration>) {
        declarations.forEach { register(it) }
    }

    /**
     * Get all channel declarations
     */
    fun declarations(): List<WebSocketChannelHandlerDeclaration> = handlers.values.toList()

    /**
     * Get channel declaration map (channelName -> Declaration)
     */
    fun declarationMap(): Map<String, WebSocketChannelHandlerDeclaration> = handlers.toMap()

    /**
     * Get declaration by channel name
     */
    fun getDeclaration(channelName: String): WebSocketChannelHandlerDeclaration? = handlers[channelName]
}
