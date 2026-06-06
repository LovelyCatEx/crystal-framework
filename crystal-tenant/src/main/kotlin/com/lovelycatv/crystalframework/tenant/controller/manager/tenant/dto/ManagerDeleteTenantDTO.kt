package com.lovelycatv.crystalframework.tenant.controller.manager.tenant.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

 data class ManagerDeleteTenantDTO(
    override val ids: List<Long>
) : BaseManagerDeleteDTO(ids)
