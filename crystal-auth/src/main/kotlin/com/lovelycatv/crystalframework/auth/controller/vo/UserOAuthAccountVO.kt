package com.lovelycatv.crystalframework.auth.controller.vo

data class UserOAuthAccountVO(
    val id: Long,
    val platformId: Int,
    val nickname: String?,
    val avatar: String?,
)