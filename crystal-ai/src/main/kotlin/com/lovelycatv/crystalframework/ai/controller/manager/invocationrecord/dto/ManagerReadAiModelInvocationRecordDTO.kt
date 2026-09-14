package com.lovelycatv.crystalframework.ai.controller.manager.invocationrecord.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.database.QueryNode

data class ManagerReadAiModelInvocationRecordDTO(
    override val page: Int,
    override val pageSize: Int,
    override val id: Long? = null,
    override val query: QueryNode? = null,
) : BaseManagerReadDTO(page, pageSize)
