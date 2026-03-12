package com.lovelycatv.crystalframework.tenant.entity

import com.lovelycatv.crystalframework.shared.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

@Table("tenant_roles")
class TenantRoleEntity(
    id: Long = 0,
    @Column(value = "tenant_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var tenantId: Long = 0,
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
) : BaseEntity(id, createdTime, modifiedTime, deletedTime)
