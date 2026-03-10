package com.lovelycatv.crystalframework.tenant.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.lovelycatv.crystalframework.shared.entity.BaseEntity
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.tenant.types.TenantStatus
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

@Table("tenants")
class TenantEntity(
    id: Long = 0,
    @Column(value = "owner_user_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var ownerUserId: Long = 0,
    @Column(value = "name")
    var name: String = "",
    @Column(value = "description")
    var description: String? = null,
    @Column(value = "icon")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var icon: Long? = null,
    @Column(value = "status")
    var status: Int = TenantStatus.REVIEWING.ordinal,
    @Column(value = "tire_type_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var tireTypeId: Long = 0,
    @Column(value = "subscribed_time")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var subscribedTime: Long = 0,
    @Column(value = "expires_time")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var expiresTime: Long = 0,
    @Column(value = "contact_name")
    var contactName: String = "",
    @Column(value = "settings")
    var settings: String? = null,
    @Column(value = "contact_email")
    var contactEmail: String = "",
    @Column(value = "contact_phone")
    var contactPhone: String = "",
    @Column(value = "address")
    var address: String = "",
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseEntity(id, createdTime, modifiedTime, deletedTime) {
    @JsonIgnore
    fun getRealStatus(): TenantStatus {
        return TenantStatus.entries.getOrNull(this.status)
            ?: throw BusinessException("invalid tenant status id $status")
    }
}
