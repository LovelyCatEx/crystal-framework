package com.lovelycatv.crystalframework.ai.controller.manager.usergroupmodel.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.database.QueryNode

data class ManagerReadAiUserGroupModelDTO(
    override val page: Int = 1,
    override val pageSize: Int = 20,
    override val id: Long? = null,
    override val query: QueryNode? = null,
    val userGroupId: Long? = null,
    val modelId: Long? = null
) : BaseManagerReadDTO(page, pageSize)
