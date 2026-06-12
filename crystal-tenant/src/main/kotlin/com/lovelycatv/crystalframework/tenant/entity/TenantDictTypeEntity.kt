package com.lovelycatv.crystalframework.tenant.entity

import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.types.entity.BaseScopedEntity
import com.lovelycatv.crystalframework.shared.types.tenant.DictTypeStatus
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("tenant_dict_type")
class TenantDictTypeEntity(
    id: Long = 0,
    scope: Int = ResourceScope.TENANT.typeId,
    scopeId: Long = 0,
    @Column(value = "code")
    var code: String = "",
    @Column(value = "name")
    var name: String = "",
    @Column(value = "remark")
    var remark: String? = null,
    @Column(value = "status")
    var status: Int = DictTypeStatus.ENABLED.typeId,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseScopedEntity(id, scope, scopeId, createdTime, modifiedTime, deletedTime) {

    fun getRealStatus(): DictTypeStatus? = DictTypeStatus.getById(status)
}
