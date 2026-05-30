package com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO

data class ManagerUpdateTenantTireBenefitValueDTO(
    override val id: Long,
    val tireTypeId: Long? = null,
    val featureId: Long? = null,
    val featureValue: String? = null,
) : BaseManagerUpdateDTO(id)
