package com.lovelycatv.crystalframework.approval.controller.manager.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadScopedDTO
import com.lovelycatv.crystalframework.shared.database.QueryNode

data class ManagerReadApprovalFlowDefinitionDTO(
    override val page: Int,
    override val pageSize: Int,
    override val scope: Int = 0,
    override val scopeId: Long = 0,
    override val id: Long? = null,
    override val query: QueryNode? = null,
) : BaseManagerReadScopedDTO(page, pageSize, scope, scopeId)
