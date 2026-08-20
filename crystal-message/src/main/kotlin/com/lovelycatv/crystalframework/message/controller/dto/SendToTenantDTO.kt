package com.lovelycatv.crystalframework.message.controller.dto

import jakarta.validation.constraints.NotBlank

/**
 * Payload for an end user proactively contacting a tenant's customer-service desk.
 * The message is sent as the acting user (sender = USER) toward the tenant
 * (target = TENANT) inside the tenant scope; the fan-out reaches only members
 * holding the reception permission. [tenantId] is a Long serialized as String (per
 * the framework's Long-as-String rule); the acting user id is never trusted from
 * the client — it is derived server-side from the authenticated user. [contentType]
 * is a [com.lovelycatv.crystalframework.message.types.ContentType] typeId, defaulting
 * to TEXT when null.
 */
data class SendToTenantDTO(
    @field:NotBlank(message = "tenantId must not be blank")
    val tenantId: String = "",
    @field:NotBlank(message = "content must not be blank")
    val content: String = "",
    val contentType: Int? = null,
)
