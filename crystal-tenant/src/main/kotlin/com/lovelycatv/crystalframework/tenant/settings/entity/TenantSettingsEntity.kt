/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.settings.entity

import com.lovelycatv.crystalframework.shared.annotations.NotQueryable
import com.lovelycatv.crystalframework.shared.types.tenant.entity.BaseTenantEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("tenant_settings")
class TenantSettingsEntity(
    id: Long = 0,
    tenantId: Long = 0,
    @Column(value = "config_key")
    var configKey: String = "",
    @Column(value = "config_value")
    @field:NotQueryable
    var configValue: String? = null,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null,
) : BaseTenantEntity(id, tenantId, createdTime, modifiedTime, deletedTime)
