package com.lovelycatv.template.springboot.rbac.controller.manager.role.dto

import com.lovelycatv.template.springboot.shared.controller.dto.BaseManagerDeleteDTO

data class ManagerDeleteRoleDTO(
    override val id: Long
) : BaseManagerDeleteDTO(id)
