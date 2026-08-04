package com.lovelycatv.crystalframework.resource.service.api

import com.lovelycatv.crystalframework.resource.entity.FileResourceEntity
import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.service.FileResourceService
import com.lovelycatv.crystalframework.resource.service.api.result.FileUploadResult
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
    private val fileResourceService: FileResourceService
) {
    private val logger = logger()

    fun getStorageProvider(): StorageProviderType {
        return this.storageProvider.getRealStorageProviderType()
    }

    open fun buildObjectKey(fileType: ResourceFileType, fileNameWithExtension: String): String {
        return "${fileType.name.lowercase()}/$fileNameWithExtension"
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
     * has already authorized the viewer at mint time; each provider decides how to honor [visibility]:
     * PUBLIC resources may return a stable URL, while non-public resources must return a short-lived
     * credential (HMAC signature for local files, vendor pre-signed GET URL for cloud storage) so a leaked
     * link stops working after its TTL even if the bucket/CDN allows direct reads.
     */
    abstract suspend fun buildDownloadUrl(
        entity: FileResourceEntity,
        visibility: ResourceVisibility
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

        val existing = fileResourceService.getByMD5(md5)
        if (existing != null) {
            logger.info("File $fileNameWithExtension already exists with md5 $md5, upload skipped, entity: ${existing.toJSONString()}")

            return FileUploadResult(
                success = true,
                providerType = getStorageProvider(),
                fileType = fileType,
                objectKey = existing.objectKey,
                fileResourceEntity = existing,
                exception = null,
            )
        }

        val objectKey = this.buildObjectKey(fileType, canonicalFileName)

        val fileName = canonicalFileName.substringBeforeLast('.')
        val fileExtension = canonicalExtension

        val result = this.doUploadFile(
            fileType,
            fileLength,
            detectedMimeType,
            canonicalFileName,
            uploadStream,
            objectKey,
            progressReporter
        )

        return if (result == null) {
            val fileResourceEntity = fileResourceService.getRepository().save(
                FileResourceEntity(
                    id = fileResourceService.generateNextSnowId(),
                    scope = scope.typeId,
                    scopeId = scopeId,
                    userId = userId,
                    type = fileType.typeId,
                    fileName = fileName,
                    fileExtension = fileExtension,
                    md5 = md5,
                    fileSize = fileLength,
                    storageProviderId = storageProvider.id,
                    objectKey = objectKey
                ) newEntity true
            ).awaitFirstOrNull()

            FileUploadResult(
                success = true,
                providerType = getStorageProvider(),
                fileType = fileType,
                objectKey = objectKey,
                fileResourceEntity = fileResourceEntity,
                exception = null,
            )
        } else {
            FileUploadResult(
                success = false,
                providerType = getStorageProvider(),
                fileType = fileType,
                objectKey = objectKey,
                fileResourceEntity = null,
                exception = result,
            )
        }
    }

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
    }
}