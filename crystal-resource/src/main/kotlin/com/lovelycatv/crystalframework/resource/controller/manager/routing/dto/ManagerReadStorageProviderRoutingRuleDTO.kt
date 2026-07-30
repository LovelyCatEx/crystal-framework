package com.lovelycatv.crystalframework.resource.controller.manager.routing.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.database.QueryNode

data class ManagerReadStorageProviderRoutingRuleDTO(
    override val page: Int,
    override val pageSize: Int,
    override val id: Long? = null,
    override val query: QueryNode? = null,
    val enabled: Boolean? = null,
) : BaseManagerReadDTO(page, pageSize)
