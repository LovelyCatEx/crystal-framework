package com.lovelycatv.crystalframework.resource.controller

import com.lovelycatv.crystalframework.resource.entity.FileResourceEntity
import com.lovelycatv.crystalframework.resource.service.FileResourceService
import com.lovelycatv.crystalframework.resource.service.StorageProviderService
import com.lovelycatv.crystalframework.resource.service.api.FileResourceServiceManager
import com.lovelycatv.crystalframework.resource.service.api.impl.LocalFileResourceServiceImpl
import com.lovelycatv.crystalframework.resource.types.StorageProviderType
import com.lovelycatv.crystalframework.shared.annotations.Unauthorized
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/file")
class LocalFileResourceController(
    private val fileResourceService: FileResourceService,
    private val storageProviderService: StorageProviderService,
    private val fileResourceServiceManager: FileResourceServiceManager
) {
    @Unauthorized
    @GetMapping("/local/{fileId}")
    suspend fun readLocalFile(
        @PathVariable fileId: Long
    ): ResponseEntity<Resource> {
        val fileResourceEntity = fileResourceService.getByIdOrThrow(fileId)

        val storageProvider = storageProviderService.getByIdOrThrow(fileResourceEntity.storageProviderId)

        if (storageProvider.getRealStorageProviderType() != StorageProviderType.LOCAL_FILE_SYSTEM) {
            throw BusinessException("This endpoint only supports local file system storage")
        }

        val localService = fileResourceServiceManager.getService(storageProvider)
                as? LocalFileResourceServiceImpl
            ?: throw BusinessException("Failed to get local file resource service")

        val file = localService.getFile(fileResourceEntity.objectKey)
            ?: throw BusinessException("File not found: ${fileResourceEntity.objectKey}")

        return buildFileResponse(file, fileResourceEntity)
    }

    private fun buildFileResponse(
        file: File,
        fileResourceEntity: FileResourceEntity
    ): ResponseEntity<Resource> {
        val resource = FileSystemResource(file)

        val contentType = try {
            java.nio.file.Files.probeContentType(file.toPath())
                ?: "application/octet-stream"
        } catch (e: Exception) {
            "application/octet-stream"
        }

        val encodedFileName = URLEncoder.encode(
            "${fileResourceEntity.fileName}.${fileResourceEntity.fileExtension}",
            StandardCharsets.UTF_8
        ).replace("+", "%20")

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, contentType)
            .header(HttpHeaders.CONTENT_LENGTH, file.length().toString())
            .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
            .body(resource)
    }
}
