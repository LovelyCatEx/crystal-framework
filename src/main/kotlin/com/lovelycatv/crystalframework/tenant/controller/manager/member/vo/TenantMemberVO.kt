package com.lovelycatv.crystalframework.tenant.controller.manager.member.vo

import com.fasterxml.jackson.annotation.JsonIgnore
import com.lovelycatv.crystalframework.tenant.entity.TenantMemberEntity
import com.lovelycatv.crystalframework.tenant.types.TenantMemberStatus
import com.lovelycatv.crystalframework.user.entity.UserEntity
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

data class TenantMemberVO(
    @get:JsonSerialize(using = ToStringSerializer::class)
    val id: Long,
    @get:JsonSerialize(using = ToStringSerializer::class)
    val tenantId: Long,
    @get:JsonSerialize(using = ToStringSerializer::class)
    val memberUserId: Long,
    val status: Int,
    val createdTime: Long,
    val modifiedTime: Long,
    val deletedTime: Long?,
    val user: UserInfoVO?
) {
    companion object {
        fun fromEntity(entity: TenantMemberEntity, userEntity: UserEntity?): TenantMemberVO {
            return TenantMemberVO(
                id = entity.id,
                tenantId = entity.tenantId,
                memberUserId = entity.memberUserId,
                status = entity.status,
                createdTime = entity.createdTime,
                modifiedTime = entity.modifiedTime,
                deletedTime = entity.deletedTime,
                user = userEntity?.let { UserInfoVO.fromEntity(it) }
            )
        }
    }

    @JsonIgnore
    fun getRealStatus(): TenantMemberStatus {
        return TenantMemberStatus.entries.getOrNull(this.status)
            ?: TenantMemberStatus.INACTIVE
    }
}

