package com.lovelycatv.crystalframework.resource.entity

import com.lovelycatv.crystalframework.resource.types.StorageProviderType
import com.lovelycatv.crystalframework.shared.entity.BaseEntity
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.utils.parseObject
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("storage_providers")
class StorageProviderEntity(
    id: Long = 0,
    @Column(value = "name")
    var name: String = "",
    @Column(value = "description")
    var description: String? = null,
    @Column(value = "type")
    var type: Int = 0,
    @Column(value = "base_url")
    var baseUrl: String = "",
    @Column(value = "properties")
    var properties: String = "",
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseEntity() {
    fun getRealStorageProviderType(): StorageProviderType {
        return StorageProviderType.getByTypeId(this.type)
            ?: throw BusinessException("storage provider type ${this.type} not found")
    }

    fun getPropertiesMap(): Map<String, String?> {
        return properties.parseObject()
    }
}