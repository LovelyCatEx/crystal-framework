/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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
