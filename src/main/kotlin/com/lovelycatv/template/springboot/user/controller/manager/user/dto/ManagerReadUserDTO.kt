package com.lovelycatv.template.springboot.user.controller.manager.user.dto

import com.lovelycatv.template.springboot.shared.controller.dto.BaseManagerReadDTO

data class ManagerReadUserDTO(
    override val page: Int,
    override val pageSize: Int,
    override val id: Long? = null,
    override val searchKeyword: String? = null
) : BaseManagerReadDTO(page, pageSize)
