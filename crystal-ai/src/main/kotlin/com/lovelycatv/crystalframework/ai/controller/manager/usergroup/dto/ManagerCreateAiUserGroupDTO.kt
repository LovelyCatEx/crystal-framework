package com.lovelycatv.crystalframework.ai.controller.manager.usergroup.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.math.BigDecimal

data class ManagerCreateAiUserGroupDTO(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 128, message = "Name length cannot exceed 128 characters")
    val name: String,
    @field:NotBlank(message = "Key is required")
    @field:Size(max = 128, message = "Key length cannot exceed 128 characters")
    val key: String,
    @field:Size(max = 512, message = "Description length cannot exceed 512 characters")
    val description: String? = null,
    @field:DecimalMin(value = "0.0", inclusive = false)
    val billingMultiplier: BigDecimal = BigDecimal.ONE,
    val enabled: Boolean = true,
    val isDefault: Boolean = false,
    val sort: Int = 0,
)
