package com.lovelycatv.crystalframework.resource.controller

import com.lovelycatv.crystalframework.encrypt.annotations.EncryptedResponseData
import com.lovelycatv.crystalframework.resource.service.FileResourceService
import com.lovelycatv.crystalframework.resource.service.ResourceAccessService
import com.lovelycatv.crystalframework.resource.service.StorageProviderService
import com.lovelycatv.crystalframework.resource.service.api.FileResourceServiceManager
import com.lovelycatv.crystalframework.resource.service.api.impl.LocalFileResourceServiceImpl
import com.lovelycatv.crystalframework.resource.types.StorageProviderType
import com.lovelycatv.crystalframework.resource.utils.ResourceUrlSigner
import com.lovelycatv.crystalframework.shared.annotations.Unauthorized
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.types.common.ResourceVisibility
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.File

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/file")
class LocalFileResourceController(
    private val fileResourceService: FileResourceService,
    private val storageProviderService: StorageProviderService,
    private val fileResourceServiceManager: FileResourceServiceManager,
    private val resourceAccessService: ResourceAccessService,
    private val resourceUrlSigner: ResourceUrlSigner,
) {
    @Unauthorized
    @GetMapping("/local/{fileId}")
    @EncryptedResponseData(disabled = true)
    suspend fun readLocalFile(
        @PathVariable fileId: Long,
        @RequestParam(value = "exp", required = false) exp: Long?,
        @RequestParam(value = "sig", required = false) sig: String?,
        userAuthentication: UserAuthentication?,
    ): ResponseEntity<Resource> {
        val fileResourceEntity = fileResourceService.getByIdOrThrow(fileId)

        // Access gate: PUBLIC is served to anyone; anything else must present either a valid
        // short-lived HMAC signature (for <img> tags) or a bearer token that passes the policy.
        val isPublic = resourceAccessService.resolveVisibility(fileResourceEntity.getRealResourceFileType()) == ResourceVisibility.PUBLIC
        if (!isPublic) {
            val signatureValid = exp != null && sig != null && resourceUrlSigner.verify(fileId, exp, sig)
            if (!signatureValid) {
                resourceAccessService.assertReadable(fileResourceEntity, userAuthentication)
            }
        }

        val storageProvider = storageProviderService.getByIdOrThrow(fileResourceEntity.storageProviderId)

        if (storageProvider.getRealStorageProviderType() != StorageProviderType.LOCAL_FILE_SYSTEM) {
            throw BusinessException("This endpoint only supports local file system storage")
        }

        val localService = fileResourceServiceManager.getService(storageProvider)
                as? LocalFileResourceServiceImpl
            ?: throw BusinessException("Failed to get local file resource service")

        val file = localService.getFile(fileResourceEntity.objectKey)
            ?: throw BusinessException("File not found: ${fileResourceEntity.objectKey}")

        return buildFileResponse(file, isPublic)
    }

    private fun buildFileResponse(
        file: File,
        isPublic: Boolean
    ): ResponseEntity<Resource> {
        val resource = FileSystemResource(file)

        val contentType = try {
            java.nio.file.Files.probeContentType(file.toPath())
                ?: "application/octet-stream"
        } catch (_: Exception) {
            "application/octet-stream"
        }

        // Public assets may be shared-cached; non-public ones are per-viewer and must not be
        // stored by shared caches even though the signed URL itself is short-lived.
        val cacheControl = if (isPublic) "public, max-age=86400" else "private, no-store"

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, contentType)
            .header(HttpHeaders.CONTENT_LENGTH, file.length().toString())
            .header(HttpHeaders.CACHE_CONTROL, cacheControl)
            .body(resource)
    }
}
