/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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
