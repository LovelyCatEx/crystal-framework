package com.lovelycatv.template.springboot.rbac.controller.manager.permission.dto

import com.lovelycatv.template.springboot.shared.controller.dto.BaseManagerReadDTO

data class ManagerReadPermissionDTO(
    override val page: Int,
    override val pageSize: Int,
    override val id: Long?,
    override val searchKeyword: String?,
    val type: Int?
) : BaseManagerReadDTO(page, pageSize)