/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO
import jakarta.validation.constraints.NotNull

data class ManagerUpdateTenantDepartmentMemberDTO(
    override val id: Long,

    @field:NotNull(message = "Department ID is required")
    val departmentId: Long,

    @field:NotNull(message = "Member ID is required")
    val memberId: Long,

    @field:NotNull(message = "Role type is required")
    val roleType: Int
) : BaseManagerUpdateDTO(id)
