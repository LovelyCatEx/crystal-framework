package com.lovelycatv.crystalframework.rbac.controller.manager.permission.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

data class ManagerDeletePermissionDTO(
    override val id: Long
) : BaseManagerDeleteDTO(id)
