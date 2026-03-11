package com.lovelycatv.crystalframework.tenant.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.lovelycatv.crystalframework.shared.entity.BaseEntity
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.tenant.types.TenantPermissionType
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("tenant_permissions")
class TenantPermissionEntity(
    id: Long = 0,
    @Column(value = "name")
    var name: String = "",
    @Column(value = "description")
    var description: String? = null,
    @Column(value = "type")
    var type: Int = TenantPermissionType.ACTION.typeId,
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
    @JsonIgnore
    fun getRealPermissionType(): TenantPermissionType {
        return TenantPermissionType.getById(this.type)
            ?: throw BusinessException("invalid tenant permission type id $type")
    }
}

