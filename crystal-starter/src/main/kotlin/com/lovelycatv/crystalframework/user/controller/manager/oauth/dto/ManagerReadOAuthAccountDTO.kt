package com.lovelycatv.crystalframework.user.controller.manager.oauth.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.database.QueryNode

data class ManagerReadOAuthAccountDTO(
    override val page: Int,
    override val pageSize: Int,
    override val id: Long? = null,
    override val query: QueryNode? = null,
    val platform: Int? = null
) : BaseManagerReadDTO(page, pageSize)
