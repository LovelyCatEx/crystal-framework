package com.lovelycatv.crystalframework.mail.entity

import com.lovelycatv.crystalframework.shared.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("mail_templates")
class MailTemplateEntity(
    id: Long = 0,
    @Column(value = "type_id")
    var typeId: Long = 0,
    @Column(value = "name")
    var name: String = "",
    @Column(value = "description")
    var description: String? = null,
    @Column(value = "title")
    var title: String = "",
    @Column(value = "content")
    var content: String = "",
    @Column(value = "active")
    var active: Boolean = true,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseEntity(id, createdTime, modifiedTime, deletedTime)