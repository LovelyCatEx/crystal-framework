package com.lovelycatv.crystalframework.messagechannel.websocket.handler

import com.lovelycatv.crystalframework.messagechannel.websocket.WebSocketChannelHandler
import com.lovelycatv.crystalframework.messagechannel.websocket.types.WsAuthContext
import com.lovelycatv.crystalframework.messagechannel.websocket.types.WsInboundMessage
import com.lovelycatv.crystalframework.messagechannel.websocket.types.WsOutboundMessage
import com.lovelycatv.crystalframework.shared.constants.WebSocketChannelConstants
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Crystal Framework authenticated channel handler
 *
 * Simple echo handler that sends back whatever is received (requires authentication).
 */
@Component
class CrystalFrameworkChannelHandler : WebSocketChannelHandler {

    override val channelName = WebSocketChannelConstants.CRYSTAL_FRAMEWORK
    override val requiresAuth = true

    override fun handle(authContext: WsAuthContext?, message: WsInboundMessage): Mono<WsOutboundMessage> {
        return Mono.just(WsOutboundMessage(
            channel = channelName,
            type = "message",
            payload = mapOf(
                "pri" to System.currentTimeMillis(),
                "auth" to authContext,
                "data" to  message.payload
            )
        ))
    }
}
