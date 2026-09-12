package com.lovelycatv.crystalframework.messagechannel.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import com.lovelycatv.crystalframework.messagechannel.websocket.service.WebSocketAuthRateLimitService
import com.lovelycatv.crystalframework.messagechannel.websocket.types.WsAuthContext
import com.lovelycatv.crystalframework.messagechannel.websocket.types.WsInboundMessage
import com.lovelycatv.crystalframework.messagechannel.websocket.types.WsOutboundMessage
import com.lovelycatv.crystalframework.shared.api.system.SystemModuleClient
import com.lovelycatv.crystalframework.shared.auth.JWTSignKeyProvider
import com.lovelycatv.crystalframework.shared.exception.TooManyRequestsException
import com.lovelycatv.crystalframework.shared.utils.JwtUtil
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.ObjectProvider
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 * WebSocket Gateway unified entry point
 *
 * Responsibilities:
 * 1. Check if WebSocket is enabled via system settings
 * 2. Decide whether to authenticate based on Handler's requiresAuth
 * 3. Parse messages and route to corresponding Handler
 * 4. Handle errors and return structured error messages
 */
@Component
class WebSocketGatewayHandler(
    handlerProvider: ObjectProvider<WebSocketChannelHandler>,
    private val objectMapper: ObjectMapper,
    private val jwtSignKeyProvider: JWTSignKeyProvider,
    private val systemModuleClient: SystemModuleClient,
    private val webSocketAuthRateLimitService: WebSocketAuthRateLimitService,
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
        // Check if WebSocket is enabled via system settings
        val systemSettings = systemModuleClient.getSystemSettings()
        if (systemSettings?.module?.webSocketEnabled == false) {
            logger.warn("WebSocket connection rejected: WebSocket is disabled in system settings")
            return sendErrorAndClose(session, "WebSocket service is currently disabled")
        }

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
            // Extract IP for rate limiting
            val ip = session.handshakeInfo.remoteAddress?.address?.hostAddress ?: "unknown"

            // Authentication required but token is invalid -> reject
            if (token.isNullOrBlank()) {
                // Check IP-only rate limit (no account available)
                try {
                    runBlocking {
                        webSocketAuthRateLimitService.checkAllowedIpOnly(ip)
                    }
                } catch (e: TooManyRequestsException) {
                    val retryAfter = e.context?.retryAfterSeconds ?: 0
                    sendErrorAndClose(session, "${e.message} (retry after ${retryAfter}s)").subscribe()
                    logger.warn("WebSocket auth rate limit exceeded for IP $ip (no token)")
                    return null
                }

                sendErrorAndClose(session, "Authentication required: token missing").subscribe()
                logger.warn("WebSocket auth required for channel ${handler.channelName}, but no token provided")
                return null
            }

            return try {
                val claims = JwtUtil.parseToken(jwtSignKeyProvider.getSignKey(), token)

                val userId = claims["userId"]?.toString()?.toLongOrNull()
                val username = claims.subject
                val tenantId = claims["tenantId"]?.toString()?.toLongOrNull()

                // Build account key early for rate limiting
                val accountKey = buildAccountKey(username, tenantId)

                if (userId == null) {
                    // Record failure with known account
                    runBlocking {
                        webSocketAuthRateLimitService.recordFailure(accountKey)
                    }
                    sendErrorAndClose(session, "Authentication failed: invalid userId").subscribe()
                    logger.warn("WebSocket userId is missing or invalid for account $accountKey")
                    return null
                }

                val tenantMemberId = claims["tenantMemberId"]?.toString()?.toLongOrNull()

                if ((tenantId != null && tenantMemberId == null) || (tenantId == null && tenantMemberId != null)) {
                    // Record failure with known account
                    runBlocking {
                        webSocketAuthRateLimitService.recordFailure(accountKey)
                    }
                    sendErrorAndClose(session, "Authentication failed: tenant mismatch").subscribe()
                    logger.warn("WebSocket tenant authentication mismatch for account $accountKey")
                    return null
                }

                // Check rate limit before allowing authentication
                try {
                    runBlocking {
                        webSocketAuthRateLimitService.checkAllowed(ip, accountKey)
                    }
                } catch (e: TooManyRequestsException) {
                    val retryAfter = e.context?.retryAfterSeconds ?: 0
                    sendErrorAndClose(session, "${e.message} (retry after ${retryAfter}s)").subscribe()
                    logger.warn("WebSocket auth rate limit exceeded for account $accountKey from IP $ip")
                    return null
                }

                // Authentication successful
                val authContext = WsAuthContext(
                    userId = userId,
                    username = username,
                    tenantId = tenantId,
                    tenantMemberId = tenantMemberId,
                    tokenIssuedAt = claims.issuedAt?.time
                )

                // Record success to clear failure counters
                runBlocking {
                    webSocketAuthRateLimitService.recordSuccess(accountKey)
                }

                authContext
            } catch (e: Exception) {
                // Token parsing failed - check IP-only rate limit (username might be unavailable)
                try {
                    runBlocking {
                        webSocketAuthRateLimitService.checkAllowedIpOnly(ip)
                    }
                } catch (rateLimitEx: TooManyRequestsException) {
                    val retryAfter = rateLimitEx.context?.retryAfterSeconds ?: 0
                    sendErrorAndClose(session, "${rateLimitEx.message} (retry after ${retryAfter}s)").subscribe()
                    logger.warn("WebSocket auth rate limit exceeded for IP $ip (token parse failed)")
                    return null
                }

                // Try to extract username for recordFailure
                val username = runCatching {
                    JwtUtil.parseToken(jwtSignKeyProvider.getSignKey(), token).subject
                }.getOrNull()

                if (username != null) {
                    val accountKey = buildAccountKey(username, null)
                    runBlocking {
                        webSocketAuthRateLimitService.recordFailure(accountKey)
                    }
                }

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

    /**
     * Builds the account rate-limit key `username:tenantId`.
     * Uses "0" as tenant segment when tenantId is null (system-level auth).
     */
    private fun buildAccountKey(username: String, tenantId: Long?): String {
        val tenantSegment = tenantId?.toString() ?: "0"
        return "$username:$tenantSegment"
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
