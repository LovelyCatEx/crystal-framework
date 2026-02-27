package com.lovelycatv.crystalframework.rbac.controller.manager.role.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

data class ManagerDeleteRoleDTO(
    override val id: Long
) : BaseManagerDeleteDTO(id)
