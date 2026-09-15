/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.auth.controller.dto

import jakarta.validation.constraints.NotNull

data class UnbindTenantOAuthAccountDTO(
    @field:NotNull(message = "OAuth account ID is required")
    val oauthAccountId: Long
)
