package com.lovelycatv.crystalframework.tenant.controller.manager

import jakarta.validation.constraints.NotNull

open class BaseManagerCreateTenantResourceDTO(
    @field:NotNull(message = "Tenant ID is required")
    open val tenantId: Long,
)