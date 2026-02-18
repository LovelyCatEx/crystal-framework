package com.lovelycatv.template.springboot.user.controller.vo

import jakarta.validation.constraints.Email

data class RequestResetPasswordEmailCodeDTO(
    @Email
    val email: String
)
