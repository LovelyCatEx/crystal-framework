package com.lovelycatv.crystalframework.tenant.controller.manager.messagechannel.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import jakarta.validation.constraints.Size

/**
 * Each updatable field is nullable so the same DTO supports partial updates: change name,
 * toggle enabled, replace config (as a JSON object string), or any combination.
 *
 * channelType is intentionally absent — it is fixed at create time.
 */
data class ManagerUpdateTenantMessageChannelDTO(
    override val id: Long,

    @field:Size(max = 64, message = "name length cannot exceed 64 characters")
    val name: String? = null,

    val enabled: Boolean? = null,

    val config: String? = null,
) : BaseManagerUpdateDTO(id)
