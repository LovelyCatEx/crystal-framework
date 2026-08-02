package com.lovelycatv.crystalframework.user.controller.manager.dto

import jakarta.validation.constraints.NotBlank

data class ManagerBanUserDTO(
    val userId: Long,
    @field:NotBlank(message = "reason must not be blank")
    val reason: String,
    val banUntil: Long? = null,
)
