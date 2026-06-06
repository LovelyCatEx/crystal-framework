package com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.database.QueryNode

data class ManagerReadTenantTireBenefitValueDTO(
    override val page: Int,
    override val pageSize: Int,
    override val id: Long? = null,
    override val query: QueryNode? = null,
    val tireTypeId: Long? = null,
    val featureId: Long? = null,
) : BaseManagerReadDTO(page, pageSize)
