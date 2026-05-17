package com.lovelycatv.crystalframework.user.controller.vo

data class UserOAuthAccountVO(
    val id: Long,
    val platformId: Int,
    val nickname: String?,
    val avatar: String?,
)
