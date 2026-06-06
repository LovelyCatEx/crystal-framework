package com.lovelycatv.crystalframework.shared.controller.dto

open class BaseManagerReadTenantResourceDTO(
    override val page: Int,
    override val pageSize: Int,
    open val tenantId: Long?,
) : BaseManagerReadDTO(page, pageSize)
