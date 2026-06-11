package com.lovelycatv.crystalframework.tenant.controller.manager.dict.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

data class ManagerDeleteTenantDictTypeDTO(
    override val ids: List<Long>
) : BaseManagerDeleteDTO(ids)
