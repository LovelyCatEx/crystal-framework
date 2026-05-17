package com.lovelycatv.crystalframework.tenant.controller.manager.tire.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

 data class ManagerDeleteTenantTireTypeDTO(
    override val ids: List<Long>
) : BaseManagerDeleteDTO(ids)
