package com.lovelycatv.crystalframework.tenant.controller.vo

import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

/** Minimal member identity exposed by the tenant-internal messaging directory. */
data class TenantMateVO(
    @get:JsonSerialize(using = ToStringSerializer::class)
    val userId: Long,
    val nickname: String,
)
