package com.lovelycatv.crystalframework.user.controller.manager.user.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO

data class ManagerReadUserDTO(
    override val page: Int,
    override val pageSize: Int,
    override val id: Long? = null,
    override val searchKeyword: String? = null,
    override val startTime: Long? = null,
    override val endTime: Long? = null,
    val username: String? = null,
    val email: String? = null,
    val nickname: String? = null,
) : BaseManagerReadDTO(page, pageSize)
