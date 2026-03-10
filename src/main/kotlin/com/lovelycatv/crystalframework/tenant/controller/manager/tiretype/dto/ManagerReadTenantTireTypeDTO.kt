package com.lovelycatv.crystalframework.tenant.controller.manager.tiretype.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO

 data class ManagerReadTenantTireTypeDTO(
    override val page: Int,
    override val pageSize: Int,
    override val id: Long? = null,
    override val searchKeyword: String? = null
) : BaseManagerReadDTO(page, pageSize)
