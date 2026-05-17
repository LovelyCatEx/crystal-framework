package com.lovelycatv.crystalframework.user.controller.manager.oauth.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO

data class ManagerReadOAuthAccountDTO(
    override val page: Int,
    override val pageSize: Int,
    override val id: Long? = null,
    override val searchKeyword: String? = null,
    val platform: Int? = null
) : BaseManagerReadDTO(page, pageSize)
