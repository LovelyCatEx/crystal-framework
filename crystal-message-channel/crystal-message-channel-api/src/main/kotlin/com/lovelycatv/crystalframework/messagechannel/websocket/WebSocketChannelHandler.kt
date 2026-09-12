package com.lovelycatv.crystalframework.messagechannel.websocket

import com.lovelycatv.crystalframework.messagechannel.websocket.types.WsAuthContext
import com.lovelycatv.crystalframework.messagechannel.websocket.types.WsInboundMessage
import com.lovelycatv.crystalframework.messagechannel.websocket.types.WsOutboundMessage
import reactor.core.publisher.Mono

/**
 * WebSocket channel handler interface
 *
 * Implement this interface to handle WebSocket messages for a specific channel.
 */
interface WebSocketChannelHandler {
    /**
     * Channel name (unique identifier)
     */
    val channelName: String

    /**
     * Whether authentication is required (default true)
     *
     * - true: Must provide valid token, authContext is guaranteed non-null
     * - false: Allow anonymous access, authContext may be null
     */
    val requiresAuth: Boolean get() = true

    /**
     * Handle message and return response
     *
     * @param authContext Authentication context, null when anonymous
     * @param message Inbound message
     * @return Mono<WsOutboundMessage> Response message to send back
     */
    fun handle(authContext: WsAuthContext?, message: WsInboundMessage): Mono<WsOutboundMessage>
}
