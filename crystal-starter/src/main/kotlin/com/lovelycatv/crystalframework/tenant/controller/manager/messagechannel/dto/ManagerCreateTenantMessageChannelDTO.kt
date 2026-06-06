package com.lovelycatv.crystalframework.tenant.controller.manager.messagechannel.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerCreateTenantResourceDTO
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

/**
 * Front-end submits the channel-specific config as a JSON object string. The service
 * deserializes it into the appropriate [com.lovelycatv.crystalframework.messagechannel
 * .types.config.ChannelConfig] subtype based on [channelType].
 */
data class ManagerCreateTenantMessageChannelDTO(
    override val tenantId: Long,

    @field:NotNull(message = "channelType is required")
    @field:Positive(message = "channelType must be positive")
    val channelType: Int,

    @field:NotBlank(message = "name is required")
    @field:Size(max = 64, message = "name length cannot exceed 64 characters")
    val name: String,

    val enabled: Boolean = true,

    @field:NotBlank(message = "config is required")
    val config: String,
) : BaseManagerCreateTenantResourceDTO(tenantId)
