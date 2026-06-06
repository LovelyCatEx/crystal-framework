package com.lovelycatv.crystalframework.rbac.tenant.controller.manager.role.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerDeleteDTO

 data class ManagerDeleteTenantRoleDTO(
    override val ids: List<Long>
) : BaseManagerDeleteDTO(ids)
