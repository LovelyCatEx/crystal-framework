package com.lovelycatv.crystalframework.audit.entity

import com.lovelycatv.crystalframework.shared.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("audit_manager_logs")
class AuditLogEntity(
    id: Long = 0,
    @Column("user_id")
    val userId: Long = 0,
    @Column("username")
    val username: String = "",
    @Column("tenant_id")
    val tenantId: Long? = null,
    @Column("action")
    val action: Int = 0,
    @Column("resource_type")
    val resourceType: String = "",
    @Column("resource_ids")
    val resourceIds: String? = null,
    @Column("request_id")
    val requestId: Long? = null,
    @Column("http_method")
    val httpMethod: String? = null,
    @Column("path")
    val path: String? = null,
    @Column("remote_ip")
    val remoteIp: String? = null,
    @Column("user_agent")
    val userAgent: String? = null,
    @Column("success")
    val success: Boolean = true,
    @Column("error_message")
    val errorMessage: String? = null,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseEntity(id, createdTime, modifiedTime, deletedTime)
