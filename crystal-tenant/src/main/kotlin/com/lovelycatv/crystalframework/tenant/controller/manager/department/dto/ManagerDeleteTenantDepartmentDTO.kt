package com.lovelycatv.crystalframework.tenant.controller.manager.department.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

 data class ManagerDeleteTenantDepartmentDTO(
    override val ids: List<Long>
) : BaseManagerDeleteDTO(ids)
