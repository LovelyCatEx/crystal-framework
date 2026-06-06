package com.lovelycatv.crystalframework.auth.entity

import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("user_login_logs")
class UserLoginLogEntity(
    id: Long = 0,
    @Column("user_id")
    val userId: Long? = null,
    @Column("username")
    val username: String? = null,
    @Column("tenant_id")
    val tenantId: Long? = null,
    @Column("login_method")
    val loginMethod: Int = 0,
    @Column("oauth2_type")
    val oauth2Type: Int? = null,
    @Column("oauth2_username")
    val oauth2Username: String? = null,
    @Column("oauth2_account_id")
    val oauth2AccountId: Long? = null,
    @Column("success")
    val success: Boolean = true,
    @Column("error_message")
    val errorMessage: String? = null,
    @Column("remote_ip")
    val remoteIp: String? = null,
    @Column("user_agent")
    val userAgent: String? = null,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseEntity(id, createdTime, modifiedTime, deletedTime)