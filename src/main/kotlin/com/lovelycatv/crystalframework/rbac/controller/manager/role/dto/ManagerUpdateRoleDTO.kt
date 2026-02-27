package com.lovelycatv.crystalframework.rbac.controller.manager.role.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO

data class ManagerUpdateRoleDTO(
    override val id: Long,
    val name: String? = null,
    val description: String? = null
) : BaseManagerUpdateDTO(id)
