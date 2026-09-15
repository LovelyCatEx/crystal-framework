/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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
