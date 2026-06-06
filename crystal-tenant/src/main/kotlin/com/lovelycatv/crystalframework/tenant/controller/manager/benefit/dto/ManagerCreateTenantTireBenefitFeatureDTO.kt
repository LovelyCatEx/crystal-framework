package com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto

data class ManagerCreateTenantTireBenefitFeatureDTO(
    val featureKey: String = "",
    val name: String = "",
    val description: String? = null,
    val featureType: Int = 0,
    val defaultValue: String? = null,
)
