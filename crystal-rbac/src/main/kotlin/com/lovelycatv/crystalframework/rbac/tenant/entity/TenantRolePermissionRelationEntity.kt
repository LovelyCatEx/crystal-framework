/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.rbac.tenant.entity

import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

@Table("tenant_role_permission_relations")
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
