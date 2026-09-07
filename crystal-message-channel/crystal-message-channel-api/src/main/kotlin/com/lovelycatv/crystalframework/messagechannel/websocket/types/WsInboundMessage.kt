package com.lovelycatv.crystalframework.messagechannel.websocket.types

/**
 * WebSocket inbound message (client -> server)
 *
 * @property channel Channel name (required)
 * @property action Action type (subscribe / unsubscribe / send, etc.)
 * @property payload Business data (optional, parsed by specific handler)
 */
data class WsInboundMessage(
    val channel: String,
    val action: String,
    val payload: Map<String, Any?>? = null
)
