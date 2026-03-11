package com.lovelycatv.crystalframework.tenant.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.lovelycatv.crystalframework.shared.entity.BaseEntity
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.tenant.types.TenantMemberStatus
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

@Table("tenant_members")
class TenantMemberEntity(
    id: Long = 0,
    @Column(value = "tenant_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var tenantId: Long = 0,
    @Column(value = "member_user_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var memberUserId: Long = 0,
    @Column(value = "status")
    var status: Int = TenantMemberStatus.INACTIVE.ordinal,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseEntity(id, createdTime, modifiedTime, deletedTime) {
    @JsonIgnore
    fun getRealStatus(): TenantMemberStatus {
        return TenantMemberStatus.entries.getOrNull(this.status)
            ?: throw BusinessException("invalid tenant member status id $status")
    }
}
