package com.lovelycatv.crystalframework.tenant.controller.manager.permission.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO

 data class ManagerReadTenantPermissionDTO(
    override val page: Int,
    override val pageSize: Int,
    override val id: Long? = null,
    override val searchKeyword: String? = null,
    val type: Int? = null
) : BaseManagerReadDTO(page, pageSize)
