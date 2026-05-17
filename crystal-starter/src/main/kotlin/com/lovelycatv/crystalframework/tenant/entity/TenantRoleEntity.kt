package com.lovelycatv.crystalframework.tenant.entity

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

@Table("tenant_roles")
class TenantRoleEntity(
    id: Long = 0,
    tenantId: Long = 0,
    @Column(value = "name")
    var name: String = "",
    @Column(value = "description")
    var description: String? = null,
    @Column(value = "parent_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var parentId: Long? = null,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseTenantEntity(id, tenantId, createdTime, modifiedTime, deletedTime)
