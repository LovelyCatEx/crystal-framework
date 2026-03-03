package com.lovelycatv.crystalframework.user.controller.manager.oauth.dto

data class ManagerCreateOAuthAccountDTO(
    val userId: Long? = null,
    val platform: Int,
    val identifier: String,
    val nickname: String? = null,
    val avatar: String? = null
)
