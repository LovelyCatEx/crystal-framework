/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.controller.vo

import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

data class TenantMemberProfileVO(
    @get:JsonSerialize(using = ToStringSerializer::class)
    val id: Long,
    @get:JsonSerialize(using = ToStringSerializer::class)
    val tenantId: Long,
    @get:JsonSerialize(using = ToStringSerializer::class)
    val tenantMemberId: Long,
    @get:JsonSerialize(using = ToStringSerializer::class)
    val memberUserId: Long,
    val name: String,
    val phone: String,
    val nickname: String?,
    val avatar: String?,
    val email: String?,
    val bio: String?,
    val gender: Int?,
    @get:JsonSerialize(using = ToStringSerializer::class)
    val birthday: Long?,
    val timezone: String?,
    val locale: String?,
    @get:JsonSerialize(using = ToStringSerializer::class)
    val createdTime: Long,
    @get:JsonSerialize(using = ToStringSerializer::class)
    val modifiedTime: Long,
)
