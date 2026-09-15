/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.user.controller.manager.dto

import jakarta.validation.constraints.NotEmpty

data class ManagerForceLogoutUsersDTO(
    @field:NotEmpty(message = "userIds must not be empty")
    val userIds: List<Long>
)
