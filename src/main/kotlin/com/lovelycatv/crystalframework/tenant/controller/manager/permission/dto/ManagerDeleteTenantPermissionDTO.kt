package com.lovelycatv.crystalframework.tenant.controller.manager.permission.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

 data class ManagerDeleteTenantPermissionDTO(
    override val ids: List<Long>
) : BaseManagerDeleteDTO(ids)
