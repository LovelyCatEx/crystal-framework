package com.lovelycatv.crystalframework.tenant.controller.manager.benefit.vo

import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

data class ManagerReadTenantTireBenefitOverviewItemVO(
    val featureId: Long,
    val featureKey: String,
    val name: String,
    val description: String?,
    val featureType: Int,
    val defaultValue: String?,
    val value: String?,
    @get:JsonSerialize(using = ToStringSerializer::class)
    val valueId: Long?,
    val isCustomized: Boolean,
)
