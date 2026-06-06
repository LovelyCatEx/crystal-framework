package com.lovelycatv.crystalframework.mail.entity

import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("mail_send_logs")
class MailSendLogEntity(
    id: Long = 0,
    @Column("from_email")
    val fromEmail: String = "",
    @Column("to_email")
    val toEmail: String = "",
    @Column("subject")
    val subject: String = "",
    @Column("content")
    val content: String = "",
    @Column("success")
    val success: Boolean = true,
    @Column("error_message")
    val errorMessage: String? = null,
    @Column("user_id")
    val userId: Long? = null,
    @Column("tenant_id")
    val tenantId: Long? = null,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseEntity(id, createdTime, modifiedTime, deletedTime)