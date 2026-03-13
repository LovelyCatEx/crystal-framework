package com.lovelycatv.crystalframework.tenant.controller.vo

import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

data class TenantProfileVO(
    @get:JsonSerialize(using = ToStringSerializer::class)
    var tenantId: Long,
    @get:JsonSerialize(using = ToStringSerializer::class)
    var ownerUserId: Long?,
    var name: String,
    var description: String?,
    var icon: String?,
    var status: Int,
    @get:JsonSerialize(using = ToStringSerializer::class)
    var tireTypeId: Long?,
    @get:JsonSerialize(using = ToStringSerializer::class)
    var subscribedTime: Long?,
    @get:JsonSerialize(using = ToStringSerializer::class)
    var expiresTime: Long?,
    var contactName: String?,
    var contactEmail: String?,
    var contactPhone: String?,
    var address: String,
    @get:JsonSerialize(using = ToStringSerializer::class)
    var createdTime: Long,
    @get:JsonSerialize(using = ToStringSerializer::class)
    var modifiedTime: Long
)
