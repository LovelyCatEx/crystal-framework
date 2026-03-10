package com.lovelycatv.crystalframework.tenant.entity

import com.lovelycatv.crystalframework.shared.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

@Table("tenantrole_permission_relations")
class TenantRolePermissionRelationEntity(
    id: Long = 0,
    @Column(value = "role_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var roleId: Long = 0,
    @Column(value = "permission_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var permissionId: Long = 0,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseEntity(id, createdTime, modifiedTime, deletedTime)
