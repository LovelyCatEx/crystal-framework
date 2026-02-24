package com.lovelycatv.template.springboot.user.controller.manager.user.dto

data class ManagerCreateUserDTO(
    val username: String,
    val password: String,
    val email: String,
    val nickname: String
)
