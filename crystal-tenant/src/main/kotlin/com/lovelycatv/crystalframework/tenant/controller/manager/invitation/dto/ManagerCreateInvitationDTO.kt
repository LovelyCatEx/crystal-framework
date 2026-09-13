/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.controller.manager.invitation.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerCreateTenantResourceDTO
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class ManagerCreateInvitationDTO(
    override val tenantId: Long,

    var creatorMemberId: Long? = null,

    val departmentId: Long? = null,

    @field:NotNull(message = "Invitation count is required")
    @field:Min(value = 1)
    @field:Max(value = 9999)
    val invitationCount: Int,

    val expiresTime: Long? = null,

    val requiresReviewing: Boolean = false
) : BaseManagerCreateTenantResourceDTO(tenantId)
