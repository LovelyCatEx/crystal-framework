package com.lovelycatv.crystalframework.tenant.settings.entity

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
    var configValue: String? = null,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null,
) : BaseTenantEntity(id, tenantId, createdTime, modifiedTime, deletedTime)
