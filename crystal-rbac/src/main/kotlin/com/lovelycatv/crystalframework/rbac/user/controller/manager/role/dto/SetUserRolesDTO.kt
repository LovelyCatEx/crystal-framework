/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.rbac.user.controller.manager.role.dto

import jakarta.validation.constraints.NotNull

data class SetUserRolesDTO(
    @field:NotNull(message = "User ID is required")
    val userId: Long,

    val roleIds: List<Long>
)