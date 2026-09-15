/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.user.controller.manager.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class ManagerCreateOAuthAccountDTO(
    val userId: Long? = null,

    @field:NotNull(message = "Platform is required")
    val platform: Int,

    @field:NotBlank(message = "Identifier is required")
    @field:Size(max = 256, message = "Identifier length cannot exceed 256 characters")
    val identifier: String,

    @field:Size(max = 128, message = "Nickname length cannot exceed 128 characters")
    val nickname: String? = null,

    @field:Size(max = 256, message = "Avatar length cannot exceed 256 characters")
    val avatar: String? = null
)
