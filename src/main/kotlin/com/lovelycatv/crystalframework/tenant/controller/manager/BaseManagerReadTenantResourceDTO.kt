package com.lovelycatv.crystalframework.tenant.controller.manager

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

open class BaseManagerReadTenantResourceDTO(
    override val page: Int,
    override val pageSize: Int,
    @field:NotNull(message = "Tenant ID is required")
    open val tenantId: Long,
) : BaseManagerReadDTO(page, pageSize)
