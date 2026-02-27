package com.lovelycatv.crystalframework.rbac.controller.manager.permission.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO

data class ManagerUpdatePermissionDTO(
    override val id: Long,
    val name: String? = null,
    val description: String? = null,
    val type: Int? = null,
    val path: String? = null
) : BaseManagerUpdateDTO(id)