package com.lovelycatv.crystalframework.tenant.entity

import com.lovelycatv.crystalframework.shared.types.tenant.DictTypeStatus
import com.lovelycatv.crystalframework.shared.types.tenant.entity.BaseTenantEntity
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("tenant_dict_type")
class TenantDictTypeEntity(
    id: Long = 0,
    tenantId: Long = 0,
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
) : BaseTenantEntity(id, tenantId, createdTime, modifiedTime, deletedTime) {

    fun getRealStatus(): DictTypeStatus? = DictTypeStatus.getById(status)
}
