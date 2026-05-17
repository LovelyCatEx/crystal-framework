package com.lovelycatv.crystalframework.tenant.entity

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

@Table("tenant_invitations")
class TenantInvitationEntity(
    id: Long = 0,
    tenantId: Long = 0,
    @Column(value = "creator_member_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var creatorMemberId: Long = 0,
    @Column(value = "department_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var departmentId: Long? = null,
    @Column(value = "invitation_code")
    var invitationCode: String = "",
    @Column(value = "invitation_count")
    var invitationCount: Int = 0,
    @Column(value = "expires_time")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var expiresTime: Long? = null,
    @Column(value = "requires_reviewing")
    var requiresReviewing: Boolean = false,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseTenantEntity(id, tenantId, createdTime, modifiedTime, deletedTime)