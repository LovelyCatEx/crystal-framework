/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.controller.manager.invitation.dto

import com.lovelycatv.crystalframework.shared.database.QueryNode
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadTenantResourceDTO

data class ManagerReadInvitationDTO(
    override val page: Int,
    override val pageSize: Int,
    override val tenantId: Long? = null,
    override val id: Long? = null,
    override val query: QueryNode? = null,
) : BaseManagerReadTenantResourceDTO(page, pageSize, tenantId)
