package com.lovelycatv.crystalframework.user.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.lovelycatv.crystalframework.shared.entity.BaseEntity
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.user.types.OAuthPlatform
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

@Table("oauth_accounts")
class OAuthAccountEntity(
    id: Long = 0,
    @Column(value = "user_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var userId: Long? = null,
    @Column(value = "platform")
    var platform: Int = 0,
    @Column(value = "identifier")
    var identifier: String = "",
    @Column(value = "nickname")
    var nickname: String? = null,
    @Column(value = "avatar")
    var avatar: String? = null,
    @Column(value = "email")
    var email: String? = null,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseEntity(id, createdTime, modifiedTime, deletedTime) {
    @JsonIgnore
    fun getRealPlatform(): OAuthPlatform {
        return OAuthPlatform.getByTypeId(this.platform)
            ?: throw BusinessException("could not get oauth_platform ${this.platform}")
    }
}