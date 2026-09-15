/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.controller.manager.invitation.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import jakarta.validation.constraints.NotNull

data class ManagerUpdateInvitationDTO(
    @field:NotNull(message = "ID is required")
    override val id: Long,

    var creatorMemberId: Long? = null,

    val departmentId: Long? = null,

    val invitationCount: Int? = null,

    val expiresTime: Long? = null,

    val requiresReviewing: Boolean? = null
) : BaseManagerUpdateDTO(id)