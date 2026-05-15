package com.lovelycatv.crystalframework.audit.controller.manager.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO

data class ManagerReadAuditLogDTO(
    override val page: Int,
    override val pageSize: Int,
    override val id: Long? = null,
    override val searchKeyword: String? = null,
    override val startTime: Long? = null,
    override val endTime: Long? = null,
    val userId: Long? = null,
    val username: String? = null,
    val action: Int? = null,
    val path: String? = null,
    val remoteIp: String? = null
) : BaseManagerReadDTO(page, pageSize)
