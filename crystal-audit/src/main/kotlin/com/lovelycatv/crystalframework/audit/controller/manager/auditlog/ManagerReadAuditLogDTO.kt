package com.lovelycatv.crystalframework.audit.controller.manager.auditlog

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.database.QueryNode

data class ManagerReadAuditLogDTO(
    override val page: Int,
    override val pageSize: Int,
    override val id: Long? = null,
    override val query: QueryNode? = null,
    val userId: Long? = null,
    val username: String? = null,
    val action: Int? = null,
    val path: String? = null,
    val remoteIp: String? = null
) : BaseManagerReadDTO(page, pageSize)
