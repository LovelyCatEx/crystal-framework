package com.lovelycatv.crystalframework.tenant.controller.manager.tiretype.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

 data class ManagerDeleteTenantTireTypeDTO(
    override val ids: List<Long>
) : BaseManagerDeleteDTO(ids)
