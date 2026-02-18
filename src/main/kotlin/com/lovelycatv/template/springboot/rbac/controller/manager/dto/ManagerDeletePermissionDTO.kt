package com.lovelycatv.template.springboot.rbac.controller.manager.dto

import com.lovelycatv.template.springboot.shared.controller.dto.BaseManagerDeleteDTO

data class ManagerDeletePermissionDTO(
    override val id: Long
) : BaseManagerDeleteDTO(id)
