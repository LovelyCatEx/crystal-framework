package com.lovelycatv.crystalframework.user.controller.dto

import jakarta.validation.constraints.Email

data class ResetEmailDTO(
    val emailCode: String,
    @Email
    val newEmail: String
)
