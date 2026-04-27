package com.lovelycatv.crystalframework.tenant.controller.manager.member.dto

import com.lovelycatv.crystalframework.tenant.controller.manager.BaseManagerReadTenantResourceDTO

data class ManagerReadTenantMemberDTO(
    override val page: Int,
    override val pageSize: Int,
    override val tenantId: Long? = null,
    override val id: Long? = null,
    override val searchKeyword: String? = null,
    val status: Int? = null
) : BaseManagerReadTenantResourceDTO(page, pageSize, tenantId)
