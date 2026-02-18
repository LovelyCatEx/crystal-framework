package com.lovelycatv.template.springboot.rbac.controller.manager.dto

import com.lovelycatv.template.springboot.shared.controller.dto.BaseManagerUpdateDTO

data class ManagerUpdatePermissionDTO(
    override val id: Long,
    val name: String? = null,
    val description: String? = null,
    val type: Int? = null,
    val path: String? = null
) : BaseManagerUpdateDTO(id)