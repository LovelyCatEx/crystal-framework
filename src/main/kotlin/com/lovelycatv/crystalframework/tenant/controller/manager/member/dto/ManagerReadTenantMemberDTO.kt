package com.lovelycatv.crystalframework.tenant.controller.manager.member.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import jakarta.validation.constraints.NotNull

data class ManagerReadTenantMemberDTO(
    override val page: Int,
    override val pageSize: Int,
    override val id: Long? = null,
    override val searchKeyword: String? = null,

    val tenantId: Long? = null,

    val status: Int? = null
) : BaseManagerReadDTO(page, pageSize)
