package com.lovelycatv.template.springboot.rbac.controller.manager.role.dto

import com.lovelycatv.template.springboot.shared.controller.dto.BaseManagerUpdateDTO

data class ManagerUpdateRoleDTO(
    override val id: Long,
    val name: String? = null,
    val description: String? = null
) : BaseManagerUpdateDTO(id)
