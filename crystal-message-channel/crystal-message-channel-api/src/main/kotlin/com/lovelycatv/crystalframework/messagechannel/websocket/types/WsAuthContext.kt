package com.lovelycatv.crystalframework.messagechannel.websocket.types

/**
 * WebSocket authentication context
 *
 * Parsed from JWT token, contains user identity and tenant information.
 */
data class WsAuthContext(
    val userId: Long,
    val username: String,
    val tenantId: Long?,
    val tenantMemberId: Long?,
    val tokenIssuedAt: Long?
)
