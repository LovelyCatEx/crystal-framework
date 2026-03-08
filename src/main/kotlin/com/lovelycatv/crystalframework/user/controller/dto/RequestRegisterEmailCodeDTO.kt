package com.lovelycatv.crystalframework.user.controller.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RequestRegisterEmailCodeDTO(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    @field:Size(max = 256, message = "Email length cannot exceed 256 characters")
    val email: String
)
