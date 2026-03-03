package com.lovelycatv.crystalframework.user.controller.manager.oauth.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO

data class ManagerUpdateOAuthAccountDTO(
    override val id: Long,
    val userId: Long? = null,
    val platform: Int? = null,
    val identifier: String? = null,
    val nickname: String? = null,
    val avatar: String? = null
) : BaseManagerUpdateDTO(id)
