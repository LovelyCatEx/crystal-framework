package com.lovelycatv.template.springboot.rbac.entity

import com.lovelycatv.template.springboot.rbac.types.PermissionType
import com.lovelycatv.template.springboot.shared.entity.BaseEntity
import com.lovelycatv.template.springboot.shared.exception.BusinessException
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("user_permissions")
class UserPermissionEntity(
    id: Long = 0,
    @Column(value = "name")
    var name: String = "",
    @Column(value = "description")
    var description: String? = null,
    @Column(value = "type")
    var type: Int = PermissionType.ACTION.typeId,
    @Column(value = "path")
    var path: String? = null,
    @Column(value = "preserved_1")
    var preserved1: Int? = null,
    @Column(value = "preserved_2")
    var preserved2: Int? = null,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseEntity(id, createdTime, modifiedTime, deletedTime) {
    fun getRealPermissionType(): PermissionType {
        return PermissionType.getById(this.type)
             ?: throw BusinessException("invalid permission type id $type")
    }
}
