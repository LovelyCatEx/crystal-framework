package com.lovelycatv.crystalframework.messagechannel.websocket.types

/**
 * WebSocket outbound message (server -> client)
 *
 * @property channel Which channel this message comes from
 * @property type Message type (message / error / ack)
 * @property payload Business data
 */
data class WsOutboundMessage(
    val channel: String,
    val type: String,
    val payload: Any?
)
