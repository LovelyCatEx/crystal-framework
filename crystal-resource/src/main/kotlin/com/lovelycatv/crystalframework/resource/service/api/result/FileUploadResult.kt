package com.lovelycatv.crystalframework.resource.service.api.result

import com.lovelycatv.crystalframework.resource.entity.FileResourceEntity
import com.lovelycatv.crystalframework.resource.types.StorageProviderType
import com.lovelycatv.crystalframework.sdk.resource.file.types.ResourceFileTypeDeclaration

data class FileUploadResult(
    val success: Boolean,
    val providerType: StorageProviderType,
    val fileType: ResourceFileTypeDeclaration,
    val objectKey: String,
    val fileResourceEntity: FileResourceEntity?,
    val exception: Exception?,
)
