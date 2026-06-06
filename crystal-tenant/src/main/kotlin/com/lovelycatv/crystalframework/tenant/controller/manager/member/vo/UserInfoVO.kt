package com.lovelycatv.crystalframework.tenant.controller.manager.member.vo

import com.lovelycatv.crystalframework.user.entity.UserEntity
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

data class UserInfoVO(
    @get:JsonSerialize(using = ToStringSerializer::class)
    val id: Long,
    val username: String,
    val email: String?,
    val nickname: String,
    @get:JsonSerialize(using = ToStringSerializer::class)
    val avatar: Long?
) {
    companion object {
        fun fromEntity(entity: UserEntity): UserInfoVO {
            return UserInfoVO(
                id = entity.id,
                username = entity.username,
                email = entity.email,
                nickname = entity.nickname,
                avatar = entity.avatar
            )
        }
    }
}
