/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.system.entity

import com.lovelycatv.crystalframework.shared.annotations.NotQueryable
import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("system_settings")
class SystemSettingsEntity(
    id: Long = 0,
    @Column(value = "config_key")
    var configKey: String = "",
    @Column(value = "config_value")
    @field:NotQueryable
    var configValue: String? = null,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseEntity(id, createdTime, modifiedTime, deletedTime)