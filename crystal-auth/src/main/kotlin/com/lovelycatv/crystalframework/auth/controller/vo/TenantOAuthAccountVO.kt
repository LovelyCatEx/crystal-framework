/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.auth.controller.vo

import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

/**
 * A tenant-scoped OAuth binding owned by the current member.
 */
data class TenantOAuthAccountVO(
    @get:JsonSerialize(using = ToStringSerializer::class)
    val id: Long,
    val platformId: Int,
    val scope: Int,
    @get:JsonSerialize(using = ToStringSerializer::class)
    val tenantId: Long?,
    val nickname: String?,
    val avatar: String?,
)
