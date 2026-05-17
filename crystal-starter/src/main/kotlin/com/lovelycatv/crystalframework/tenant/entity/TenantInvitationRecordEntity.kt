package com.lovelycatv.crystalframework.tenant.entity

import com.lovelycatv.crystalframework.shared.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

@Table("tenant_invitation_records")
class TenantInvitationRecordEntity(
    id: Long = 0,
    @Column(value = "invitation_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var invitationId: Long = 0,
    @Column(value = "used_user_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var usedUserId: Long = 0,
    @Column(value = "real_name")
    var realName: String = "",
    @Column(value = "phone_number")
    var phoneNumber: String = "",
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseEntity(id, createdTime, modifiedTime, deletedTime)