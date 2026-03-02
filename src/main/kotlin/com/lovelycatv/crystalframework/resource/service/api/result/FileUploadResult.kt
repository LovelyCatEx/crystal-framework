package com.lovelycatv.crystalframework.resource.service.api.result

import com.lovelycatv.crystalframework.resource.entity.FileResourceEntity
import com.lovelycatv.crystalframework.resource.types.ResourceFileType
import com.lovelycatv.crystalframework.resource.types.StorageProviderType

data class FileUploadResult(
    val success: Boolean,
    val providerType: StorageProviderType,
    val fileType: ResourceFileType,
    val objectKey: String,
    val fileResourceEntity: FileResourceEntity?,
    val exception: Exception?,
)