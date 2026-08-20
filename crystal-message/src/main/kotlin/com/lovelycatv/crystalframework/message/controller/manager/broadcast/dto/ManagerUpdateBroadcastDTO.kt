package com.lovelycatv.crystalframework.message.controller.manager.broadcast.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import jakarta.validation.constraints.Size

/**
 * Update a broadcast. Scope is fixed at creation and cannot be changed here — the update
 * authorization resolves scope from the existing entity. Any change to `audienceType` /
 * `audienceRef` is re-validated against the fixed scope in the service.
 */
data class ManagerUpdateBroadcastDTO(
    override val id: Long,

    @field:Size(max = 256, message = "Title length cannot exceed 256 characters")
    val title: String? = null,

    val content: String? = null,

    val category: Int? = null,

    val audienceType: Int? = null,

    val audienceRef: Long? = null,

    val publishTime: Long? = null,

    val expireTime: Long? = null,
) : BaseManagerUpdateDTO(id)
