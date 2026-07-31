package com.lovelycatv.crystalframework.resource.controller.manager.routing.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class ManagerCreateStorageProviderRoutingRuleDTO(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 64, message = "Name length cannot exceed 64 characters")
    val name: String,

    val conditionTree: String? = null,

    @field:NotBlank(message = "Target provider ids is required")
    val targetProviderIds: String,

    @field:NotNull(message = "Distribution type is required")
    val distributionType: Int,

    val enabled: Boolean = true,
)
