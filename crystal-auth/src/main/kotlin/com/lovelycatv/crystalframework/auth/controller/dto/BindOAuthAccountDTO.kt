/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.auth.controller.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

 data class BindOAuthAccountDTO(
    @field:NotBlank(message = "OAuth bind token is required")
    val oauthBindToken: String,

    @field:Pattern(regexp = "^[a-zA-Z0-9_@.-]+$", message = "Username can only contain letters, numbers, underscores, hyphens, dots, and @")
    val username: String?,

    val password: String?,
)
