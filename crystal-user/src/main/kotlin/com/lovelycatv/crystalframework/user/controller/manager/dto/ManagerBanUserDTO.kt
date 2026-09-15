/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.user.controller.manager.dto

import jakarta.validation.constraints.NotBlank

data class ManagerBanUserDTO(
    val userId: Long,
    @field:NotBlank(message = "reason must not be blank")
    val reason: String,
    val banUntil: Long? = null,
)
