package com.lovelycatv.crystalframework.auth.controller.manager.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.database.QueryNode

data class ManagerReadUserLoginLogDTO(
    override val page: Int,
    override val pageSize: Int,
    override val id: Long? = null,
    override val query: QueryNode? = null,
    val userId: Long? = null,
    val username: String? = null,
    val tenantId: Long? = null,
    val loginMethod: Int? = null,
    val oauth2Type: Int? = null,
    val success: Boolean? = null,
    val remoteIp: String? = null
) : BaseManagerReadDTO(page, pageSize)
