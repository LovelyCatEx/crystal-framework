/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.controller.manager.member.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerCreateTenantResourceDTO
import jakarta.validation.constraints.NotNull

data class ManagerCreateTenantMemberDTO(
    override val tenantId: Long,

    @field:NotNull(message = "Member user ID is required")
    val memberUserId: Long,

    val status: Int? = null
) : BaseManagerCreateTenantResourceDTO(tenantId)
