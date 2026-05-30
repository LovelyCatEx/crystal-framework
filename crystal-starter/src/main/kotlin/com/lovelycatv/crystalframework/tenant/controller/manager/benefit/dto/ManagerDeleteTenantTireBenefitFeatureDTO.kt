package com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

data class ManagerDeleteTenantTireBenefitFeatureDTO(
    override val ids: List<Long>,
) : BaseManagerDeleteDTO(ids)
