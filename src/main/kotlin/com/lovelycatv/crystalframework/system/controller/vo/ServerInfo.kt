package com.lovelycatv.crystalframework.system.controller.vo

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
