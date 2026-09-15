/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.rbac.user.controller.manager.permission.dto

import jakarta.validation.constraints.NotNull

data class SetRolePermissionsDTO(
    @field:NotNull(message = "Role ID is required")
    val roleId: Long,

    val permissionIds: List<Long>
)