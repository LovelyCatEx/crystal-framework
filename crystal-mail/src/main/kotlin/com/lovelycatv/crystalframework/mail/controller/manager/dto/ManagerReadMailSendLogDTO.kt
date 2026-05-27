package com.lovelycatv.crystalframework.mail.controller.manager.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.database.QueryNode

data class ManagerReadMailSendLogDTO(
    override val page: Int,
    override val pageSize: Int,
    override val id: Long? = null,
    override val query: QueryNode? = null,
    val toEmail: String? = null,
    val success: Boolean? = null,
    val userId: Long? = null,
    val tenantId: Long? = null
) : BaseManagerReadDTO(page, pageSize)
