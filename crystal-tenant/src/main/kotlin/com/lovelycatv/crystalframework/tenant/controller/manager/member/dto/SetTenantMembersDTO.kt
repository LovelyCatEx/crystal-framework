/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.controller.manager.member.dto

import jakarta.validation.constraints.NotNull

data class SetTenantMembersDTO(
    @field:NotNull(message = "Tenant ID is required")
    val tenantId: Long,

    val userIds: List<Long>
)
