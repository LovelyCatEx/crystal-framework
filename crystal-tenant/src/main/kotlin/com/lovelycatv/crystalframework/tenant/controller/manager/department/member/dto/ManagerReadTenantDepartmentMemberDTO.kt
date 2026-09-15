/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadDTO
import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadTenantResourceDTO
import com.lovelycatv.crystalframework.shared.database.QueryNode
import jakarta.validation.constraints.NotNull

data class ManagerReadTenantDepartmentMemberDTO(
    override val page: Int,
    override val pageSize: Int,
    override val id: Long? = null,
    override val query: QueryNode? = null,

    @field:NotNull(message = "Department ID is required")
    val departmentId: Long,

    val memberId: Long? = null,
    val roleType: Int? = null
) : BaseManagerReadTenantResourceDTO(page, pageSize, null)
