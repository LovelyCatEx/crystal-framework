package com.lovelycatv.template.springboot.user.controller.dto

import jakarta.validation.constraints.Email

data class ResetPasswordDTO(
    @Email
    val email: String,
    val emailCode: String,
    val newPassword: String
)
