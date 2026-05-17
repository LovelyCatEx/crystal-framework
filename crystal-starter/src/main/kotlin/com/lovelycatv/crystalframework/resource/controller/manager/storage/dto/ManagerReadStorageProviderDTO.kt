package com.lovelycatv.crystalframework.resource.controller.manager.storage.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO

data class ManagerReadStorageProviderDTO(
    override val page: Int,
    override val pageSize: Int,
    override val id: Long? = null,
    override val searchKeyword: String? = null,
    val type: Int? = null
) : BaseManagerReadDTO(page, pageSize)
