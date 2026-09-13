/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.controller.vo

import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

/** Minimal member identity exposed by the tenant-internal messaging directory. */
data class TenantMateVO(
    @get:JsonSerialize(using = ToStringSerializer::class)
    val userId: Long,
    val nickname: String,
)
