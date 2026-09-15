/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.controller.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

/** Request a paginated directory of members from a tenant the caller belongs to. */
data class QueryTenantMatesDTO(
    @field:NotBlank(message = "tenantId must not be blank")
    val tenantId: String = "",
    @field:Min(value = 1, message = "page must be at least 1")
    val page: Int = 1,
    @field:Min(value = 1, message = "pageSize must be at least 1")
    @field:Max(value = 20, message = "pageSize must not exceed 20")
    val pageSize: Int = 20,
)
