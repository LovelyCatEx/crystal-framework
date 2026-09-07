package com.lovelycatv.crystalframework.messagechannel.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import com.lovelycatv.crystalframework.messagechannel.websocket.types.WsAuthContext
import com.lovelycatv.crystalframework.messagechannel.websocket.types.WsInboundMessage
import com.lovelycatv.crystalframework.messagechannel.websocket.types.WsOutboundMessage
import com.lovelycatv.crystalframework.shared.auth.JWTSignKeyProvider
import com.lovelycatv.crystalframework.shared.utils.JwtUtil
import com.lovelycatv.vertex.log.logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import kotlin.math.log

/**
 * WebSocket Gateway unified entry point
 *
 * Responsibilities:
 * 1. Decide whether to authenticate based on Handler's requiresAuth
 * 2. Parse messages and route to corresponding Handler
 * 3. Handle errors and return structured error messages
 */
@Component
class WebSocketGatewayHandler(
    private val handlerProvider: ObjectProvider<WebSocketChannelHandler>,
    private val objectMapper: ObjectMapper,
    private val jwtSignKeyProvider: JWTSignKeyProvider
) : WebSocketHandler {

    private val logger = logger()
    private val handlers = mutableMapOf<String, WebSocketChannelHandler>()

    init {
        // Collect all Handler implementations
        handlerProvider.orderedStream().forEach { handler ->
            handlers[handler.channelName] = handler
            logger.info("WebSocket channel registered: ${handler.channelName} (requiresAuth=${handler.requiresAuth})")
        }
    }

    override fun handle(session: WebSocketSession): Mono<Void> {
        // Try to extract token from query param (may be empty)
        val token = session.handshakeInfo.uri.rawQuery
            ?.split("&")
            ?.firstOrNull { it.startsWith("token=") }
            ?.substringAfter("token=")

        val output = session.receive()
            .concatMap { message ->
                try {
                    val dto = parseMessage(message.payloadAsText)
                    val handler = handlers[dto.channel]

                    if (handler == null) {
                        logger.warn("Unknown channel: ${dto.channel}")
                        return@concatMap WsOutboundMessage(
                            channel = "system",
                            type = "error",
                            payload = mapOf("message" to "Unknown channel: ${dto.channel}")
                        ).toMono()
                    }

                    // Decide whether to authenticate based on handler.requiresAuth
                    val authContext = authenticateIfRequired(handler, token, session)
                    if (authContext == null && handler.requiresAuth) {
                        return@concatMap Mono.empty()
                    }

                    // Route to handler
                    handler.handle(authContext, dto)
                } catch (e: Exception) {
                    logger.error("WebSocket message handling error", e)
                    WsOutboundMessage(
                        channel = "system",
                        type = "error",
                        payload = mapOf("message" to "Message handling failed: ${e.message}")
                    ).toMono()
                }
            }
            .map { outbound ->
                session.textMessage(objectMapper.writeValueAsString(outbound))
            }

        return session.send(output)
    }

    /**
     * Decide whether to authenticate based on handler's requiresAuth
     *
     * @return WsAuthContext (when authenticated or authentication not required), null means authentication failed and connection closed
     */
    private fun authenticateIfRequired(
        handler: WebSocketChannelHandler,
        token: String?,
        session: WebSocketSession
    ): WsAuthContext? {
        if (handler.requiresAuth) {
            // Authentication required but token is invalid -> reject
            if (token.isNullOrBlank()) {
                sendErrorAndClose(session, "Authentication required: token missing").subscribe()
                logger.warn("WebSocket auth required for channel ${handler.channelName}, but no token provided")
                return null
            }

            return try {
                val claims = JwtUtil.parseToken(jwtSignKeyProvider.getSignKey(), token)

                val userId = claims["userId"]?.toString()?.toLongOrNull()
                if (userId == null) {
                    sendErrorAndClose(session, "Authentication failed: invalid userId").subscribe()
                    logger.warn("WebSocket userId is missing or invalid")
                    return null
                }

                val tenantId = claims["tenantId"]?.toString()?.toLongOrNull()
                val tenantMemberId = claims["tenantMemberId"]?.toString()?.toLongOrNull()

                if ((tenantId != null && tenantMemberId == null) || (tenantId == null && tenantMemberId != null)) {
                    sendErrorAndClose(session, "Authentication failed: tenant mismatch").subscribe()
                    logger.warn("WebSocket tenant authentication mismatch")
                    return null
                }

                WsAuthContext(
                    userId = userId,
                    username = claims.subject,
                    tenantId = tenantId,
                    tenantMemberId = tenantMemberId,
                    tokenIssuedAt = claims.issuedAt?.time
                )
            } catch (e: Exception) {
                sendErrorAndClose(session, "Authentication failed: ${e.message}").subscribe()
                logger.warn("WebSocket token validation failed: ${e.message}")
                null
            }
        } else {
            // Authentication not required -> authContext is null (anonymous) or try to validate token (if provided)
            return if (token.isNullOrBlank()) {
                null // Anonymous
            } else {
                try {
                    val claims = JwtUtil.parseToken(jwtSignKeyProvider.getSignKey(), token)
                    val userId = claims["userId"]?.toString()?.toLongOrNull() ?: return null
                    val tenantId = claims["tenantId"]?.toString()?.toLongOrNull()
                    val tenantMemberId = claims["tenantMemberId"]?.toString()?.toLongOrNull()

                    WsAuthContext(
                        userId = userId,
                        username = claims.subject,
                        tenantId = tenantId,
                        tenantMemberId = tenantMemberId,
                        tokenIssuedAt = claims.issuedAt?.time
                    )
                } catch (_: Exception) {
                    null // Token invalid, but don't reject anonymous access
                }
            }
        }
    }

    private fun parseMessage(payload: String): WsInboundMessage {
        return objectMapper.readValue(payload, WsInboundMessage::class.java)
    }

    private fun sendError(session: WebSocketSession, errorMessage: String): Mono<Void> {
        val error = WsOutboundMessage(
            channel = "system",
            type = "error",
            payload = mapOf("message" to errorMessage)
        )
        return session.send(
            Mono.just(session.textMessage(objectMapper.writeValueAsString(error)))
        )
    }

    private fun sendErrorAndClose(session: WebSocketSession, errorMessage: String): Mono<Void> {
        return sendError(session, errorMessage).then(session.close())
    }
}
