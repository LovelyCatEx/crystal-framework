package com.lovelycatv.crystalframework.rbac.user.controller.manager.permission.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

data class ManagerDeletePermissionDTO(
    override val ids: List<Long>
) : BaseManagerDeleteDTO(ids)
