package com.lovelycatv.crystalframework.user.controller.dto

import jakarta.validation.constraints.Email

data class RequestResetPasswordEmailCodeDTO(
    @Email
    val email: String
)
