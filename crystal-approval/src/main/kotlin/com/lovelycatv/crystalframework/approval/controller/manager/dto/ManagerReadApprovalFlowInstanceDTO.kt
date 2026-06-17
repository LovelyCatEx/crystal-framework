package com.lovelycatv.crystalframework.approval.controller.manager.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadScopedDTO
import com.lovelycatv.crystalframework.shared.database.QueryNode

data class ManagerReadApprovalFlowInstanceDTO(
    override val page: Int,
    override val pageSize: Int,
    override val scope: Int,
    override val scopeId: Long,
    override val id: Long? = null,
    override val query: QueryNode? = null,
) : BaseManagerReadScopedDTO(page, pageSize, scope, scopeId)
