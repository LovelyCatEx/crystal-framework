package com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

data class ManagerDeleteTenantTireBenefitValueDTO(
    override val ids: List<Long>,
) : BaseManagerDeleteDTO(ids)
