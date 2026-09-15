/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.rbac.tenant.controller.manager.role.dto

import jakarta.validation.constraints.NotNull

data class SetMemberRolesDTO(
    @field:NotNull(message = "Member ID is required")
    val memberId: Long,

    val roleIds: List<Long>
)