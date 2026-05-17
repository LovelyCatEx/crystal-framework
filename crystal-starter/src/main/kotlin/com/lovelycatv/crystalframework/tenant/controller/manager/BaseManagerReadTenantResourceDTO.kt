package com.lovelycatv.crystalframework.tenant.controller.manager

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO

open class BaseManagerReadTenantResourceDTO(
    override val page: Int,
    override val pageSize: Int,
    open val tenantId: Long?,
) : BaseManagerReadDTO(page, pageSize)
