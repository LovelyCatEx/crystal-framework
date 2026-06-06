package com.lovelycatv.crystalframework.shared.types.tenant.entity

import com.lovelycatv.crystalframework.shared.types.tenant.TenantStatus
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

data class UserAuthenticatedTenantVO(
    var id: Long,
    @get:JsonSerialize(using = ToStringSerializer::class)
    var ownerUserId: Long,
    var description: String?,
    @get:JsonSerialize(using = ToStringSerializer::class)
    var icon: Long?,
    var status: Int = TenantStatus.REVIEWING.ordinal,
    @get:JsonSerialize(using = ToStringSerializer::class)
    var tireTypeId: Long,
    @get:JsonSerialize(using = ToStringSerializer::class)
    var subscribedTime: Long,
    @get:JsonSerialize(using = ToStringSerializer::class)
    var expiresTime: Long,
    var contactName: String,
    var settings: String?,
    var contactEmail: String,
    var contactPhone: String,
    var address: String,
    var createdTime: Long,
    var modifiedTime: Long,
    var deletedTime: Long?,
    var userId: Long,
    var tenantMmberId: Long,
)