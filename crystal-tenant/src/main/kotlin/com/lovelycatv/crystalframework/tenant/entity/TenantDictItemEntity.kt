package com.lovelycatv.crystalframework.tenant.entity

import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import com.lovelycatv.crystalframework.shared.types.entity.ScopedEntity
import com.lovelycatv.crystalframework.shared.types.tenant.DictItemStatus
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

@Table("tenant_dict_item")
class TenantDictItemEntity(
    id: Long = 0,
    @Column(value = "type_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var typeId: Long = 0,
    @Column(value = "item_code")
    var itemCode: String = "",
    @Column(value = "item_value")
    var itemValue: String = "",
    @Column(value = "parent_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var parentId: Long? = null,
    @Column(value = "sort_order")
    var sortOrder: Int = 0,
    @Column(value = "is_default")
    var isDefault: Boolean = false,
    @Column(value = "status")
    var status: Int = DictItemStatus.ENABLED.typeId,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseEntity(id, createdTime, modifiedTime, deletedTime), ScopedEntity<Long> {

    /**
     * DictItem's direct parent is typeId (for relationship check chain: item → type → tenant)
     */
    override fun getDirectParentId(): Long = typeId

    fun getRealStatus(): DictItemStatus? = DictItemStatus.getById(status)
}
