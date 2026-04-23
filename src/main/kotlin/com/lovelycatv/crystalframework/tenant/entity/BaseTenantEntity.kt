package com.lovelycatv.crystalframework.tenant.entity

import com.lovelycatv.crystalframework.shared.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

abstract class BaseTenantEntity(
    id: Long = 0,
    @Column(value = "tenant_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var tenantId: Long = 0,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseEntity(id, createdTime, modifiedTime, deletedTime)
