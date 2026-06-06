package com.lovelycatv.crystalframework.tenant.controller.manager.messagechannel.dto

import com.lovelycatv.crystalframework.shared.database.QueryNode
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadTenantResourceDTO

data class ManagerReadTenantMessageChannelDTO(
    override val page: Int,
    override val pageSize: Int,
    override val tenantId: Long,
    override val id: Long? = null,
    override val query: QueryNode? = null,
    val channelType: Int? = null,
) : BaseManagerReadTenantResourceDTO(page, pageSize, tenantId)
