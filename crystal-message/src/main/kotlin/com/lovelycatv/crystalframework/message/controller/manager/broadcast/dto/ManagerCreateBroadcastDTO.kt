package com.lovelycatv.crystalframework.message.controller.manager.broadcast.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerCreateScopedDTO
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * Create a broadcast. `scope + scopeId` (from [BaseManagerCreateScopedDTO]) drive both the
 * RBAC scoped authorization and the stored isolation boundary. The sender identity is derived
 * from the scope in the service (SYSTEM → system sender, TENANT → the tenant), never taken from
 * the client. `audienceType` / `audienceRef` must stay consistent with the scope — enforced in
 * the service.
 */
data class ManagerCreateBroadcastDTO(
    override val scope: Int,
    override val scopeId: Long = 0,

    val category: Int? = null,

    val audienceType: Int,

    val audienceRef: Long? = null,

    @field:NotBlank(message = "Title is required")
    @field:Size(max = 256, message = "Title length cannot exceed 256 characters")
    val title: String,

    @field:NotBlank(message = "Content is required")
    val content: String,

    val publishTime: Long? = null,

    val expireTime: Long? = null,
) : BaseManagerCreateScopedDTO(scope, scopeId)
