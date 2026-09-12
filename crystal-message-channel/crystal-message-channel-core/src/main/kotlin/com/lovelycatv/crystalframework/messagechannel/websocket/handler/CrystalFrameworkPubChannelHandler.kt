package com.lovelycatv.crystalframework.messagechannel.websocket.handler

import com.lovelycatv.crystalframework.messagechannel.websocket.WebSocketChannelHandler
import com.lovelycatv.crystalframework.messagechannel.websocket.types.WsAuthContext
import com.lovelycatv.crystalframework.messagechannel.websocket.types.WsInboundMessage
import com.lovelycatv.crystalframework.messagechannel.websocket.types.WsOutboundMessage
import com.lovelycatv.crystalframework.shared.constants.WebSocketChannelConstants
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Crystal Framework public channel handler
 *
 * Simple echo handler that sends back whatever is received (anonymous access allowed).
 */
@Component
class CrystalFrameworkPubChannelHandler : WebSocketChannelHandler {

    override val channelName = WebSocketChannelConstants.CRYSTAL_FRAMEWORK_PUB
    override val requiresAuth = false

    override fun handle(authContext: WsAuthContext?, message: WsInboundMessage): Mono<WsOutboundMessage> {
        return Mono.just(WsOutboundMessage(
            channel = channelName,
            type = "message",
            payload = mapOf(
                "pub" to System.currentTimeMillis(),
                "data" to  message.payload
            )
        ))
    }
}
