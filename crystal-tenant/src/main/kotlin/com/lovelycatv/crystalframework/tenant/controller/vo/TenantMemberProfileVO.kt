package com.lovelycatv.crystalframework.tenant.controller.vo

import com.lovelycatv.crystalframework.tenant.entity.TenantMemberProfileEntity
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
    @get:JsonSerialize(using = ToStringSerializer::class)
    val avatar: Long?,
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
) {
    companion object {
        fun fromEntity(entity: TenantMemberProfileEntity, fullAccess: Boolean = true): TenantMemberProfileVO {
            return TenantMemberProfileVO(
                id = entity.id,
                tenantId = entity.tenantId,
                tenantMemberId = entity.tenantMemberId,
                memberUserId = entity.memberUserId,
                name = entity.name,
                phone = if (fullAccess) entity.phone else "",
                nickname = entity.nickname,
                avatar = entity.avatar,
                email = if (fullAccess) entity.email else null,
                bio = entity.bio,
                gender = entity.gender,
                birthday = if (fullAccess) entity.birthday else null,
                timezone = entity.timezone,
                locale = entity.locale,
                createdTime = entity.createdTime,
                modifiedTime = entity.modifiedTime,
            )
        }
    }
}
