package com.lovelycatv.crystalframework.rbac.controller.manager.role.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO

data class ManagerReadRoleDTO(
    override val page: Int,
    override val pageSize: Int,
    override val id: Long? = null,
    override val searchKeyword: String? = null
) : BaseManagerReadDTO(page, pageSize)
