package com.lovelycatv.crystalframework.user.controller.vo

data class UserProfileVO(
    val id: Long,
    val nickname: String,
    val avatar: String?,
    val username: String?,
    val email: String?,
    val registeredTime: Long?
)
