package com.lovelycatv.crystalframework.tenant.controller.manager.benefit.vo

import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

data class ManagerReadTenantTireBenefitOverviewGroupVO(
    @get:JsonSerialize(using = ToStringSerializer::class)
    val tireTypeId: Long,
    val items: List<ManagerReadTenantTireBenefitOverviewItemVO>,
)
