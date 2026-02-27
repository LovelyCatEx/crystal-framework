package com.lovelycatv.crystalframework.rbac.entity

import com.lovelycatv.crystalframework.shared.entity.BaseEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table(name = "user_role_relations")
class UserRoleRelationEntity(
    id: Long = 0,
    @Column(value = "user_id")
    var userId: Long = 0,
    @Column(value = "role_id")
    var roleId: Long = 0,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseEntity(id, createdTime, modifiedTime, deletedTime) {

}