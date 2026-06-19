package com.lovelycatv.crystalframework.approval.controller.manager.dto

import jakarta.validation.constraints.NotNull

data class HandleApprovalFlowTaskDTO(
    @field:NotNull
    val taskId: Long? = null,
    @field:NotNull
    val approved: Boolean? = null,
    val comment: String? = null,
    val formData: String? = null,
)
