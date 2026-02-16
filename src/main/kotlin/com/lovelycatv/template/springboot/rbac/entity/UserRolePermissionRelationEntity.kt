package com.lovelycatv.template.springboot.rbac.entity

import com.lovelycatv.template.springboot.shared.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table(name = "user_role_permission_relations")
class UserRolePermissionRelationEntity(
    id: Long = 0,
    @Column(value = "role_id")
    var roleId: Long = 0,
    @Column(value = "permission_id")
    var permissionId: Long = 0,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseEntity(id, createdTime, modifiedTime, deletedTime) {

}