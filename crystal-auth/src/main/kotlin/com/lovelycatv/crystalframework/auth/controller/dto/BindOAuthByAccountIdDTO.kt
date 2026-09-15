/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.auth.controller.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class BindOAuthByAccountIdDTO(
    @field:NotBlank(message = "OAuth bind token is required")
    val oauthBindToken: String,

    @field:NotNull(message = "Binding scope is required")
    val scope: Int,
)
