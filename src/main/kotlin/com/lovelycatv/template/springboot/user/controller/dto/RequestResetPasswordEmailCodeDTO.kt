package com.lovelycatv.template.springboot.user.controller.dto

import jakarta.validation.constraints.Email

data class RequestResetPasswordEmailCodeDTO(
    @Email
    val email: String
)
