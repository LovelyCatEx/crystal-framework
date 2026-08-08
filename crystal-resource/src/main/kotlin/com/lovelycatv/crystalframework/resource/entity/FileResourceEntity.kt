package com.lovelycatv.crystalframework.resource.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.lovelycatv.crystalframework.resource.types.FileResourceStatus
import com.lovelycatv.crystalframework.resource.types.ResourceFileType
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.types.entity.BaseScopedEntity
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

@Table("file_resources")
class FileResourceEntity(
    id: Long = 0,
    scope: Int = ResourceScope.SYSTEM.typeId,
    scopeId: Long = 0,
    @Column(value = "user_id")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var userId: Long = 0,
    @Column(value = "type")
    var type: Int = ResourceFileType.USER_AVATAR.typeId,
    @Column(value = "file_name")
    var fileName: String = "",
    @Column(value = "file_extension")
    var fileExtension: String = "",
    @Column(value = "md5")
    var md5: String = "",
    @Column(value = "file_size")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var fileSize: Long = 0,
    @Column(value = "storage_provider_id")
    var storageProviderId: Long = 0,
    @Column(value = "object_key")
    var objectKey: String = "",
    @Column(value = "status")
    var status: Int = FileResourceStatus.COMMITTED.typeId,
    @Column(value = "upload_token")
    @JsonIgnore
    var uploadToken: String? = null,
    @Column(value = "lease_until")
    @JsonIgnore
    var leaseUntil: Long? = null,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseScopedEntity(id, scope, scopeId, createdTime, modifiedTime, deletedTime) {
    @JsonIgnore
    fun getRealStatus(): FileResourceStatus {
        return FileResourceStatus.getByTypeId(this.status)
            ?: throw BusinessException("file resource status ${this.status} not found")
    }

    @JsonIgnore
    fun getRealResourceFileType(): ResourceFileType {
        return ResourceFileType.getByTypeId(this.type)
            ?: throw BusinessException("resource file type ${this.type} not found")
    }

    @JsonIgnore
    fun getRealScope(): ResourceScope {
        return ResourceScope.getById(this.scope)
            ?: throw BusinessException("resource scope ${this.scope} not found")
    }
}