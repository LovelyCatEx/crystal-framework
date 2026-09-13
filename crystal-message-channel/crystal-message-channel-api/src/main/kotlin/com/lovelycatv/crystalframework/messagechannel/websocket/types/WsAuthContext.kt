/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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
