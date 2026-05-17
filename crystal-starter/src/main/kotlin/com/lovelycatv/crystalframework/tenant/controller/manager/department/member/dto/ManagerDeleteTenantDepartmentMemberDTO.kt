package com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

data class ManagerDeleteTenantDepartmentMemberDTO(
    override val ids: List<Long>
) : BaseManagerDeleteDTO(ids)
