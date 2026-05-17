package com.lovelycatv.crystalframework.mail.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.lovelycatv.crystalframework.shared.entity.BaseEntity
import com.lovelycatv.crystalframework.shared.utils.parseObject
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("mail_template_types")
class MailTemplateTypeEntity(
    id: Long = 0,
    @Column(value = "name")
    var name: String = "",
    @Column(value = "description")
    var description: String? = null,
    @Column(value = "variables")
    var variables: String = "",
    @Column(value = "category_id")
    var categoryId: Long = 0,
    @Column(value = "allow_multiple")
    var allowMultiple: Boolean = false,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseEntity(id, createdTime, modifiedTime, deletedTime) {
    @JsonIgnore
    fun getVariablesList(): List<String> {
        return this.variables.parseObject()
    }
}