package com.lovelycatv.crystalframework.system.controller.manager.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class ManagerTestSendEmailDTO(
    @field:NotBlank(message = "email must not be blank")
    @field:Email(message = "email format is invalid")
    val email: String?,
)
