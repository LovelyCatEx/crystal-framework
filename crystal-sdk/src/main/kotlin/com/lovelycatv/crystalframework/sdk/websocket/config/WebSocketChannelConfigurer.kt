/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.sdk.websocket.config

import com.lovelycatv.crystalframework.sdk.websocket.WebSocketChannelRegistry

/**
 * WebSocket channel configurer interface
 *
 * Implement this interface to register custom channel declarations.
 * Spring will automatically collect all implementations and call the configure method.
 */
interface WebSocketChannelConfigurer {
    /**
     * Configure channel declarations
     *
     * @param registry Channel registry
     */
    fun configure(registry: WebSocketChannelRegistry)
}
