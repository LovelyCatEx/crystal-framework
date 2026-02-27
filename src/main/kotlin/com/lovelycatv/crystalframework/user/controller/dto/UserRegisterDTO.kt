package com.lovelycatv.crystalframework.user.controller.dto

data class UserRegisterDTO(
    val username: String,
    val password: String,
    val email: String,
    val emailCode: String
)
