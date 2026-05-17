package com.lovelycatv.crystalframework.tenant.controller.manager.tenant.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO

 data class ManagerReadTenantDTO(
    override val page: Int,
    override val pageSize: Int,
    override val id: Long? = null,
    override val searchKeyword: String? = null,
    val status: Int? = null
) : BaseManagerReadDTO(page, pageSize)
