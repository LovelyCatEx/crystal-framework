package com.lovelycatv.crystalframework.audit.context

/**
 * Per-request snapshot captured once at the edge of the controller chain and made
 * available everywhere downstream.
 */
data class AuditRequestInfo(
    val requestId: Long,
    val httpMethod: String,
    val path: String,
    val remoteIp: String?,
    val userAgent: String?
)