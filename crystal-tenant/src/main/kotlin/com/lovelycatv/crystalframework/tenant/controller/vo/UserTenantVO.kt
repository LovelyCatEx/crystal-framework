/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.controller.vo

data class UserTenantVO(
    val tenantId: Long,
    val tenantName: String,
    val tenantAvatar: String?,
    val memberStatus: Int,
    val authenticated: Boolean,
)
