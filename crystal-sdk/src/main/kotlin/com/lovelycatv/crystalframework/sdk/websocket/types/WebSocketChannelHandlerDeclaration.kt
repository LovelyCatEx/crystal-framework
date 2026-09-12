package com.lovelycatv.crystalframework.sdk.websocket.types

/**
 * WebSocket channel handler declaration
 *
 * @property channelName Channel name (unique key)
 * @property requiresAuth Whether authentication is required (default true)
 * @property description Channel description
 */
data class WebSocketChannelHandlerDeclaration(
    val channelName: String,
    val requiresAuth: Boolean = true,
    val description: String = ""
)
