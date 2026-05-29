package com.lovelycatv.crystalframework.tenant.controller.manager.benefit.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO

data class ManagerReadTenantTireBenefitFeatureDTO(
    override val page: Int,
    override val pageSize: Int,
    override val id: Long? = null,
    override val query: com.lovelycatv.crystalframework.shared.database.QueryNode? = null,
    val featureType: Int? = null,
) : BaseManagerReadDTO(page, pageSize)
