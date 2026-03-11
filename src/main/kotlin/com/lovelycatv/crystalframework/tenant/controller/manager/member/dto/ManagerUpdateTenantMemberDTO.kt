package com.lovelycatv.crystalframework.tenant.controller.manager.member.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO

data class ManagerUpdateTenantMemberDTO(
    override val id: Long,

    val status: Int? = null
) : BaseManagerUpdateDTO(id)
