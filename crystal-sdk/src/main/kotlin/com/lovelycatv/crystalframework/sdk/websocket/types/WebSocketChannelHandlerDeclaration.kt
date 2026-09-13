/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.sdk.websocket.types

/**
 * WebSocket channel handler declaration
 *
 * @property channelName Channel name (unique key)
 * @property requiresAuth Whether authentication is required (default true)
 * @property description Channel description
 */
data class WebSocketChannelHandlerDeclaration(
    val channelName: String,
    val requiresAuth: Boolean = true,
    val description: String = ""
)
