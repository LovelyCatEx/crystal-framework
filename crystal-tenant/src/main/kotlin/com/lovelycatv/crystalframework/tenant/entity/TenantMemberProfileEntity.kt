package com.lovelycatv.crystalframework.tenant.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.types.common.Gender
import com.lovelycatv.crystalframework.shared.types.tenant.entity.BaseTenantEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

@Table("tenant_member_profiles")
class TenantMemberProfileEntity(
    id: Long = 0,
    tenantId: Long = 0,
    @Column(value = "tenant_member_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var tenantMemberId: Long = 0,
    @Column(value = "member_user_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var memberUserId: Long = 0,
    @Column(value = "name")
    var name: String = "",
    @Column(value = "phone")
    var phone: String = "",
    @Column(value = "nickname")
    var nickname: String? = null,
    @Column(value = "avatar")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var avatar: Long? = null,
    @Column(value = "email")
    var email: String? = null,
    @Column(value = "bio")
    var bio: String? = null,
    @Column(value = "gender")
    var gender: Int? = null,
    @Column(value = "birthday")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var birthday: Long? = null,
    @Column(value = "timezone")
    var timezone: String? = null,
    @Column(value = "locale")
    var locale: String? = null,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseTenantEntity(id, tenantId, createdTime, modifiedTime, deletedTime) {
    @JsonIgnore
    fun getRealGender(): Gender? {
        val typeId = this.gender ?: return null
        return Gender.getByType(typeId)
            ?: throw BusinessException("invalid gender id $typeId")
    }
}
