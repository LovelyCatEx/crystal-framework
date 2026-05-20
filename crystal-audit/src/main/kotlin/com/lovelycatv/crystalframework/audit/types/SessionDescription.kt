package com.lovelycatv.crystalframework.audit.types

data class SessionDescription(
    val sessionId: String,
    val remoteIp: String,
    val userAgent: String,
    val userId: Long,
    val tenantId: Long,
)
