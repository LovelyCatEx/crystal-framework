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
import com.lovelycatv.crystalframework.shared.utils.toJSONString
import com.lovelycatv.vertex.log.logger
import com.lovelycatv.crystalframework.resource.utils.detectMimeType
import org.springframework.http.codec.multipart.FilePart
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import org.springframework.dao.DataIntegrityViolationException
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
        // 1. Read bytes and detect MIME type from file content
        //    (caller-provided Content-Type header is ignored for security — clients can forge it)
        val byteArray = inputStream.readBytes()
        val detectedMimeType = detectMimeType(byteArray)

        // 2. Validate detected MIME type against allowed types for this fileType category
        fileResourceService.assertFileContentType(
            fileType,
            detectedMimeType
        )

        // 3. Resolve canonical file extension purely from detected MIME type.
        //    The uploader-supplied extension is ignored on purpose: e.g. the tenant-avatar cropper
        //    on the frontend re-encodes PNG as JPEG, so the incoming ".png" no longer matches the
        //    actual bytes. Only the detected content type decides the on-disk extension; the safety
        //    net is still assertFileContentType() above, which rejects anything outside the allow-list.
        val canonicalExtension = fileResourceService.resolveFileExtension(
            fileType,
            detectedMimeType,
        ) ?: throw BusinessException("No allowed file extension found for detected content type $detectedMimeType")
        val canonicalFileName = "${UUID.randomUUID()}.$canonicalExtension"

        // 4. Calculate MD5 for deduplication check
        val md5 = FileMD5Utils.calculateMD5(ByteArrayInputStream(byteArray))

        // 5. Check if file already exists by MD5 within the same scope
        //    If found, return existing entity immediately (skip upload)
        val existing = fileResourceService.getCommitedFileByMD5(md5, scope.typeId, scopeId)
        if (existing != null) {
            logger.info("File $fileNameWithExtension already exists with md5 $md5, upload skipped, entity: ${existing.toJSONString()}")
            return FileUploadResult(true, getStorageProvider(), fileType, existing.objectKey, existing, null)
        }

        // 6. Build object key and create UPLOADING entity with lease timeout
        val objectKey = this.buildObjectKey(fileType, canonicalFileName)
        val uploadingEntity = FileResourceEntity(
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

        // 7. Reserve upload slot by inserting UPLOADING entity (unique constraint on MD5+scope)
        //    If insert fails (race condition), another upload already owns this file — return existing
        val reserved = try {
            fileResourceService.reservePreUpload(uploadingEntity)
        } catch (e: DataIntegrityViolationException) {
            fileResourceService.getCommitedFileByMD5(md5, scope.typeId, scopeId)
        }
        if (reserved == null || reserved.id != uploadingEntity.id) {
            val current = reserved ?: fileResourceService.getCommitedFileByMD5(md5, scope.typeId, scopeId)
            return if (current != null) {
                FileUploadResult(true, getStorageProvider(), fileType, current.objectKey, current, null)
            } else {
                FileUploadResult(false, getStorageProvider(), fileType, objectKey, null, BusinessException("File upload is already in progress"))
            }
        }

        // 8. Perform actual upload to storage provider (OSS/COS/local)
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

        // 9. Commit upload: transition entity from UPLOADING → COMMITTED status
        //    If commit fails, delete uploaded object and mark cleanup pending
        if (!fileResourceService.commitUpload(uploadingEntity)) {
            val deleteError = this.deleteObject(objectKey)
            if (deleteError != null) {
                fileResourceService.markUploadCleanupPending(uploadingEntity)
            } else {
                fileResourceService.removeUploadRecord(uploadingEntity)
            }
            return FileUploadResult(false, getStorageProvider(), fileType, objectKey, null, BusinessException("Could not commit file resource"))
        }

        // 10. Return success with committed entity
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