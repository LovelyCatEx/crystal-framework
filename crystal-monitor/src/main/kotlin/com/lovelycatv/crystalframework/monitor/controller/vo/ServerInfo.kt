/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.monitor.controller.vo

/**
 * Server information VO
 */
data class ServerInfo(
    val serverName: String,
    val databaseVersion: String,
    val redisVersion: String,
    val projectVersion: String,
    val uptime: String
)
