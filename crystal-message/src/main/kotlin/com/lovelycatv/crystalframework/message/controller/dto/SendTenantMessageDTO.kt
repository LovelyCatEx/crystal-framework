package com.lovelycatv.crystalframework.message.controller.dto

import jakarta.validation.constraints.NotBlank

/**
 * Payload for a tenant member sending a customer-service message on their tenant's
 * behalf to an end user. [tenantId] and [targetUserId] are Longs serialized as String
 * (per the framework's Long-as-String rule); the acting member id is never trusted from
 * the client — it is derived server-side from the authenticated user, whose authority to
 * act as the tenant is enforced by the tenant party resolver. [contentType] is a
 * [com.lovelycatv.crystalframework.message.types.ContentType] typeId, defaulting to TEXT
 * when null.
 */
data class SendTenantMessageDTO(
    @field:NotBlank(message = "tenantId must not be blank")
    val tenantId: String = "",
    @field:NotBlank(message = "targetUserId must not be blank")
    val targetUserId: String = "",
    @field:NotBlank(message = "content must not be blank")
    val content: String = "",
    val contentType: Int? = null,
)
