package com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto

import com.lovelycatv.crystalframework.shared.request.PageQuery

data class ManagerReadTenantTireBenefitOverviewDTO(
    override val page: Int,
    override val pageSize: Int = 20,
    val tireTypeId: Long,
) : PageQuery(page, pageSize)
