package com.lovelycatv.crystalframework.resource.service.api

import com.lovelycatv.crystalframework.resource.entity.FileResourceEntity
import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.service.FileResourceService
import com.lovelycatv.crystalframework.resource.service.api.result.FileUploadResult
import com.lovelycatv.crystalframework.resource.types.FileResourceServiceProperties
import com.lovelycatv.crystalframework.resource.types.FileResourceStatus
import com.lovelycatv.crystalframework.resource.types.ResourceFileType
import com.lovelycatv.crystalframework.resource.types.StorageProviderType
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.types.common.ResourceVisibility
import com.lovelycatv.crystalframework.shared.utils.FileMD5Utils
import com.lovelycatv.crystalframework.shared.utils.asInputStreamWithLength
import com.lovelycatv.crystalframework.shared.utils.getContentType
import com.lovelycatv.crystalframework.shared.utils.toJSONString
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.reactive.awaitFirstOrNull
import com.lovelycatv.crystalframework.resource.utils.detectMimeType
import org.springframework.http.codec.multipart.FilePart
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.UUID

abstract class AbstractFileResourceService(
    private val storageProvider: StorageProviderEntity,
    private val fileResourceService: FileResourceService,
    /**
     * Shared object-key prefix (see [FileResourceServiceProperties.basePath]). Defaults to empty so a
     * provider without one (or the local provider, which uses basePath as its filesystem root instead)
     * produces the historical unprefixed key. Cloud factories pass their configured value here.
     */
    private val basePath: String = "",
) {
    private val logger = logger()

    fun getStorageProvider(): StorageProviderType {
        return this.storageProvider.getRealStorageProviderType()
    }

    open fun buildObjectKey(fileType: ResourceFileType, fileNameWithExtension: String): String {
        val key = "${fileType.name.lowercase()}$OBJECT_KEY_SEPARATOR$fileNameWithExtension"
        val prefix = this.basePath.trim().trim(OBJECT_KEY_SEPARATOR)
        return if (prefix.isEmpty()) key else "$prefix$OBJECT_KEY_SEPARATOR$key"
    }

    /**
     * The provider's configured base URL, guaranteed to end with a single trailing slash so it can be
     * concatenated directly with an object key. Centralized here so no impl re-implements the normalization.
     */
    protected fun normalizedProviderBaseUrl(): String {
        val baseUrl = this.storageProvider.baseUrl
        val withScheme = if (baseUrl.startsWith(HTTP_SCHEME_PREFIX) || baseUrl.startsWith(HTTPS_SCHEME_PREFIX)) {
            baseUrl
        } else {
            "$HTTPS_SCHEME_PREFIX$baseUrl"
        }
        return if (withScheme.endsWith("/")) withScheme else "$withScheme/"
    }

    /**
     * The object key without a leading slash, ready to append to [normalizedProviderBaseUrl].
     */
    protected fun normalizedObjectKey(entity: FileResourceEntity): String {
        return entity.objectKey.removePrefix("/")
    }

    /**
     * Produces a download URL for [entity] appropriate to [visibility] and this provider.
     *
     * The caller ([com.lovelycatv.crystalframework.resource.service.FileResourceService.getFileDownloadUrl])
     * has already authorized the viewer at mint time. The visibility branching lives here once so no impl
     * re-implements it: PUBLIC resources get a stable URL ([buildPublicDownloadUrl]), while non-public
     * resources get a short-lived credential ([buildSignedDownloadUrl]) so a leaked link stops working
     * after [signedUrlTtlSeconds] even if the bucket/CDN allows direct reads.
     */
    suspend fun buildDownloadUrl(
        entity: FileResourceEntity,
        visibility: ResourceVisibility,
        signedUrlTtlSeconds: Long,
    ): String {
        return if (this.storageProvider.getRealStorageProviderType() == StorageProviderType.LOCAL_FILE_SYSTEM) {
            when (visibility) {
                ResourceVisibility.PUBLIC ->
                    buildPublicDownloadUrl(entity)
                ResourceVisibility.AUTHENTICATED,
                ResourceVisibility.SCOPE_MEMBER,
                ResourceVisibility.OWNER_ONLY,
                ResourceVisibility.SYSTEM_ADMIN ->
                    buildSignedDownloadUrl(entity, signedUrlTtlSeconds)
            }
        } else {
            // For safety, force using presigned s3 url
            buildSignedDownloadUrl(entity, signedUrlTtlSeconds)
        }
    }

    /**
     * A stable, cacheable URL for a PUBLIC resource. Readable by anyone; carries no expiry.
     */
    protected abstract suspend fun buildPublicDownloadUrl(entity: FileResourceEntity): String

    /**
     * A short-lived credentialed URL for a non-public resource, valid for [signedUrlTtlSeconds] seconds
     * (HMAC signature for local files, vendor pre-signed GET URL for cloud storage).
     */
    protected abstract suspend fun buildSignedDownloadUrl(
        entity: FileResourceEntity,
        signedUrlTtlSeconds: Long,
    ): String

    suspend fun uploadFile(
        userId: Long,
        scope: ResourceScope,
        scopeId: Long,
        fileType: ResourceFileType,
        filePart: FilePart,
        targetFileName: String,
        progressReporter: ((Int) -> Unit)? = null
    ): FileUploadResult {
        val (inputStream, fileSize) = filePart.asInputStreamWithLength()

        return this.uploadFile(
            userId = userId,
            scope = scope,
            scopeId = scopeId,
            fileType = fileType,
            fileNameWithExtension = targetFileName,
            fileLength = fileSize,
            inputStream = inputStream,
            progressReporter = progressReporter
        )
    }

    suspend fun uploadFile(
        userId: Long,
        scope: ResourceScope,
        scopeId: Long,
        fileType: ResourceFileType,
        fileNameWithExtension: String,
        fileLength: Long,
        inputStream: InputStream,
        progressReporter: ((Int) -> Unit)? = null
    ): FileUploadResult {
        // Read bytes first so we can detect the actual MIME type from file content.
        // The caller-provided fileContentType is intentionally ignored for security:
        // a client can forge any Content-Type header.
        val byteArray = inputStream.readBytes()
        val detectedMimeType = detectMimeType(byteArray)

        fileResourceService.assertFileContentType(
            fileType,
            detectedMimeType
        )

        val requestedExtension = fileNameWithExtension
            .substringAfterLast('.', "")
            .lowercase()
        val canonicalExtension = fileResourceService.resolveFileExtension(
            fileType,
            detectedMimeType,
            requestedExtension
        ) ?: throw BusinessException("File extension does not match detected content type")
        val canonicalFileName = "${UUID.randomUUID()}.$canonicalExtension"

        val md5 = FileMD5Utils.calculateMD5(ByteArrayInputStream(byteArray))

        val uploadStream = ByteArrayInputStream(byteArray)

        val existing = fileResourceService.getByMD5(md5, scope.typeId, scopeId)
        if (existing != null) {
            logger.info("File $fileNameWithExtension already exists with md5 $md5, upload skipped, entity: ${existing.toJSONString()}")
            return FileUploadResult(true, getStorageProvider(), fileType, existing.objectKey, existing, null)
        }

        val objectKey = this.buildObjectKey(fileType, canonicalFileName)
        val uploadingEntity: FileResourceEntity = FileResourceEntity(
            id = fileResourceService.generateNextSnowId(),
            scope = scope.typeId,
            scopeId = scopeId,
            userId = userId,
            type = fileType.typeId,
            fileName = canonicalFileName.substringBeforeLast('.'),
            fileExtension = canonicalExtension,
            md5 = md5,
            fileSize = fileLength,
            storageProviderId = storageProvider.id,
            objectKey = objectKey,
            status = FileResourceStatus.UPLOADING.typeId,
            uploadToken = UUID.randomUUID().toString(),
            leaseUntil = System.currentTimeMillis() + UPLOAD_LEASE_MILLIS,
        )

        val reserved = try {
            fileResourceService.reserveUpload(uploadingEntity)
        } catch (e: org.springframework.dao.DataIntegrityViolationException) {
            fileResourceService.getByMD5(md5, scope.typeId, scopeId)
        }
        if (reserved == null || reserved.id != uploadingEntity.id) {
            val current = reserved ?: fileResourceService.getByMD5(md5, scope.typeId, scopeId)
            return if (current != null) {
                FileUploadResult(true, getStorageProvider(), fileType, current.objectKey, current, null)
            } else {
                FileUploadResult(false, getStorageProvider(), fileType, objectKey, null, BusinessException("File upload is already in progress"))
            }
        }

        val result = this.doUploadFile(
            fileType,
            fileLength,
            detectedMimeType,
            canonicalFileName,
            ByteArrayInputStream(byteArray),
            objectKey,
            progressReporter
        )
        if (result != null) {
            fileResourceService.removeUploadRecord(uploadingEntity)
            return FileUploadResult(false, getStorageProvider(), fileType, objectKey, null, result)
        }

        if (!fileResourceService.commitUpload(uploadingEntity)) {
            val deleteError = this.deleteObject(objectKey)
            if (deleteError != null) {
                fileResourceService.markUploadCleanupPending(uploadingEntity)
            } else {
                fileResourceService.removeUploadRecord(uploadingEntity)
            }
            return FileUploadResult(false, getStorageProvider(), fileType, objectKey, null, BusinessException("Could not commit file resource"))
        }

        uploadingEntity.status = FileResourceStatus.COMMITTED.typeId
        uploadingEntity.leaseUntil = null
        return FileUploadResult(true, getStorageProvider(), fileType, objectKey, uploadingEntity, null)
    }

    abstract suspend fun deleteObject(objectKey: String): Exception?

    protected abstract suspend fun doUploadFile(
        fileType: ResourceFileType,
        fileLength: Long,
        fileContentType: String,
        fileNameWithExtension: String,
        inputStream: InputStream,
        objectKey: String,
        progressReporter: ((Int) -> Unit)? = null
    ): Exception?

    open fun destroy() {
        // Release resources
    }

    companion object {
        private const val HTTP_SCHEME_PREFIX = "http://"

        private const val HTTPS_SCHEME_PREFIX = "https://"

        /** Milliseconds per second, for impls whose SDK expects a millisecond-based expiry. */
        protected const val MILLIS_PER_SECOND = 1000L

        private const val OBJECT_KEY_SEPARATOR = '/'

        private const val UPLOAD_LEASE_MILLIS = 10 * 60 * MILLIS_PER_SECOND
    }
}