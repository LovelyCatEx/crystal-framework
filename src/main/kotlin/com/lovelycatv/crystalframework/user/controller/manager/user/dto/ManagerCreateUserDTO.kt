package com.lovelycatv.crystalframework.user.controller.manager.user.dto

data class ManagerCreateUserDTO(
    val username: String,
    val password: String,
    val email: String,
    val nickname: String
)
