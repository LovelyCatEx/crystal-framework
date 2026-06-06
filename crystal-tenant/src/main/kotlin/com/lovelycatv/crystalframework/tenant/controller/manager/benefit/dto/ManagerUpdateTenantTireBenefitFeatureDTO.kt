package com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO

data class ManagerUpdateTenantTireBenefitFeatureDTO(
    override val id: Long,
    val featureKey: String? = null,
    val name: String? = null,
    val description: String? = null,
    val featureType: Int? = null,
    val defaultValue: String? = null,
) : BaseManagerUpdateDTO(id)
