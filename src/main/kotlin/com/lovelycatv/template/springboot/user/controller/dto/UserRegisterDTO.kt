package com.lovelycatv.template.springboot.user.controller.dto

data class UserRegisterDTO(
    val username: String,
    val password: String,
    val email: String,
    val emailCode: String
)
