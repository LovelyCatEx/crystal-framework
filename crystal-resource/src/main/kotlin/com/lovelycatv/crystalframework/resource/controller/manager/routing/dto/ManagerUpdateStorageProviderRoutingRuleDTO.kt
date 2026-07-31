package com.lovelycatv.crystalframework.resource.controller.manager.routing.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import jakarta.validation.constraints.Size

data class ManagerUpdateStorageProviderRoutingRuleDTO(
    override val id: Long,

    @field:Size(max = 64, message = "Name length cannot exceed 64 characters")
    val name: String? = null,

    val conditionTree: String? = null,
    val targetProviderIds: String? = null,
    val distributionType: Int? = null,
    val enabled: Boolean? = null,
) : BaseManagerUpdateDTO(id)
