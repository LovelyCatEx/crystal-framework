package com.lovelycatv.crystalframework.resource.controller.manager.routing.dto

import jakarta.validation.constraints.NotEmpty

data class ManagerReorderStorageProviderRoutingRuleDTO(
    @field:NotEmpty(message = "Ordered ids is required")
    val orderedIds: List<Long>
)
