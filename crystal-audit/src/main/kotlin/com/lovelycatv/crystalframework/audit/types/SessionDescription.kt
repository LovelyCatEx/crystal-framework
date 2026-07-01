package com.lovelycatv.crystalframework.audit.types

import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

data class SessionDescription(
    val sessionId: String,
    val remoteIp: String,
    val userAgent: String,
    // Non-authenticated sessions (e.g. Prometheus scrape) never hit CustomAuthFilter,
    // so AUDIT_USER_ID / AUDIT_TENANT_ID are never written into the session attributes.
    @get:JsonSerialize(using = ToStringSerializer::class)
    val userId: Long?,
    @get:JsonSerialize(using = ToStringSerializer::class)
    val tenantId: Long?,
    val type: Int,
) {
    fun getRealType(): SessionType? = SessionType.getById(type)
}
