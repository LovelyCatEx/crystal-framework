package com.lovelycatv.crystalframework.message.controller.dto

/**
 * Send a direct message from the acting user (who must be a member of [tenantId])
 * to any user, regardless of whether the target belongs to [tenantId]. This is the
 * cross-org peer send: the conversation is isolated within the sender's tenant scope,
 * but the recipient need not be a member of that scope.
 *
 * Contrast with [SendInTenantMessageDTO], which additionally enforces that the
 * target is a member of the same tenant.
 */
data class SendToMemberDTO(
    val tenantId: String,
    val targetUserId: String,
    val content: String,
    val contentType: Int? = null,
)
