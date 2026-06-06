package com.lovelycatv.crystalframework.tenant.controller.manager.member.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

data class ManagerDeleteTenantMemberDTO(
    override val ids: List<Long>
) : BaseManagerDeleteDTO(ids)
