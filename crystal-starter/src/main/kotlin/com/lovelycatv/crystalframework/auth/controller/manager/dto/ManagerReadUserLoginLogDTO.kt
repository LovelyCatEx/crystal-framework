package com.lovelycatv.crystalframework.auth.controller.manager.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO

data class ManagerReadUserLoginLogDTO(
    override val page: Int,
    override val pageSize: Int,
    override val id: Long? = null,
    override val searchKeyword: String? = null,
    override val startTime: Long? = null,
    override val endTime: Long? = null,
    val userId: Long? = null,
    val username: String? = null,
    val tenantId: Long? = null,
    val loginMethod: Int? = null,
    val oauth2Type: Int? = null,
    val success: Boolean? = null,
    val remoteIp: String? = null
) : BaseManagerReadDTO(page, pageSize)