package com.lovelycatv.crystalframework.approval.controller.manager.dto

import jakarta.validation.constraints.NotNull

class StartApprovalFlowDTO(
    @field:NotNull
    val definitionId: Long? = null,
    val formData: String = "{}",
)
