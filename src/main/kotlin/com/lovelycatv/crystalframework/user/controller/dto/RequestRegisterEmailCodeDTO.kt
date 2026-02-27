package com.lovelycatv.crystalframework.user.controller.dto

import jakarta.validation.constraints.Email

data class RequestRegisterEmailCodeDTO(
    @Email
    val email: String
)
