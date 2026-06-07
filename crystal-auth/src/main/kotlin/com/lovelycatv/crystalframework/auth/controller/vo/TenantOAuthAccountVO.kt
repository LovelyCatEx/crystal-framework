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
