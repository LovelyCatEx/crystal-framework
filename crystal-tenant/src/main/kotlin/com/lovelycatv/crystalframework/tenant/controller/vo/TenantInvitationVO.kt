/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.controller.vo

data class TenantInvitationVO(
    val tenantId: Long,
    val expiresAt: Long?,
    val departmentName: String?,
    val reachedUsageLimit: Boolean,
)
