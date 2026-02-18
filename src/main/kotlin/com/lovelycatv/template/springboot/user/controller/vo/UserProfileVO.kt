package com.lovelycatv.template.springboot.user.controller.vo

data class UserProfileVO(
    val id: Long,
    val nickname: String,
    val username: String?,
    val email: String?,
    val registeredTime: Long?
)
