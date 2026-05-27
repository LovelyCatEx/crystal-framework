package com.lovelycatv.crystalframework.resource.controller.manager.storage.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.database.QueryNode

data class ManagerReadStorageProviderDTO(
    override val page: Int,
    override val pageSize: Int,
    override val id: Long? = null,
    override val query: QueryNode? = null,
    val type: Int? = null
) : BaseManagerReadDTO(page, pageSize)
