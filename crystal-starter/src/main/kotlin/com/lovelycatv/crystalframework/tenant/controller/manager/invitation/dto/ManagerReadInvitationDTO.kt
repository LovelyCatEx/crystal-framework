package com.lovelycatv.crystalframework.tenant.controller.manager.invitation.dto

import com.lovelycatv.crystalframework.tenant.controller.manager.BaseManagerReadTenantResourceDTO

data class ManagerReadInvitationDTO(
    override val page: Int,
    override val pageSize: Int,
    override val tenantId: Long? = null,
    override val id: Long? = null,
    override val searchKeyword: String? = null,
) : BaseManagerReadTenantResourceDTO(page, pageSize, tenantId)
