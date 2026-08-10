package com.lovelycatv.crystalframework.message.controller.dto

import jakarta.validation.constraints.NotBlank

/**
 * Payload for a direct message between two members of the same tenant. Both user
 * identities are checked against the tenant scope by the messaging service.
 */
data class SendInTenantMessageDTO(
    @field:NotBlank(message = "tenantId must not be blank")
    val tenantId: String = "",
    @field:NotBlank(message = "targetUserId must not be blank")
    val targetUserId: String = "",
    @field:NotBlank(message = "content must not be blank")
    val content: String = "",
    val contentType: Int? = null,
)
