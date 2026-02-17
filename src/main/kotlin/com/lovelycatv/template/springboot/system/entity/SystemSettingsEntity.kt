package com.lovelycatv.template.springboot.system.entity

import com.lovelycatv.template.springboot.shared.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("system_settings")
class SystemSettingsEntity(
    id: Long = 0,
    @Column(value = "config_key")
    var configKey: String = "",
    @Column(value = "config_value")
    var configValue: String? = null,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseEntity(id, createdTime, modifiedTime, deletedTime) {
}