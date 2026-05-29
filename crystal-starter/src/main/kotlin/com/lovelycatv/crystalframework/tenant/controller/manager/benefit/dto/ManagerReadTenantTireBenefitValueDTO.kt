package com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO

data class ManagerReadTenantTireBenefitValueDTO(
    override val page: Int,
    override val pageSize: Int,
    override val id: Long? = null,
    override val query: com.lovelycatv.crystalframework.shared.database.QueryNode? = null,
    val tireTypeId: Long? = null,
    val featureId: Long? = null,
) : BaseManagerReadDTO(page, pageSize)
