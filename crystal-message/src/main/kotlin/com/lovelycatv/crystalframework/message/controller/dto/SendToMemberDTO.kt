package com.lovelycatv.crystalframework.message.controller.dto

/**
 * Send a direct message between two members of the same tenant. The endpoint enforces that
 * BOTH the acting user and [targetUserId] are members of [tenantId]; the resulting conversation
 * is isolated within that TENANT scope. This is the only member-to-member peer channel — strict
 * scope isolation forbids cross-scope peer chats, so contacting a user outside your shared tenant
 * must instead go through the tenant's service desk.
 */
data class SendToMemberDTO(
    val tenantId: String,
    val targetUserId: String,
    val content: String,
    val contentType: Int? = null,
)
