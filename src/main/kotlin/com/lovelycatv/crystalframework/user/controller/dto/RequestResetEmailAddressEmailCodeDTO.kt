package com.lovelycatv.crystalframework.user.controller.dto

import jakarta.validation.constraints.Email

data class RequestResetEmailAddressEmailCodeDTO(
    @Email
    val newEmail: String
)
