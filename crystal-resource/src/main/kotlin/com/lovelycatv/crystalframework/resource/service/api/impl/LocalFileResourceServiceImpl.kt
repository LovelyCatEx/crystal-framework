package com.lovelycatv.crystalframework.resource.service.api.impl

import com.lovelycatv.crystalframework.resource.entity.FileResourceEntity
import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.service.FileResourceService
import com.lovelycatv.crystalframework.resource.service.api.AbstractFileResourceService
import com.lovelycatv.crystalframework.resource.types.ResourceFileType
import com.lovelycatv.crystalframework.resource.utils.ResourceUrlSigner
import com.lovelycatv.crystalframework.shared.api.system.SystemModuleClient
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.types.common.ResourceVisibility
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class LocalFileResourceServiceImpl(
    storageProvider: StorageProviderEntity,
    fileResourceService: FileResourceService,
    private val basePath: String,
    private val systemModuleClient: SystemModuleClient,
    private val resourceUrlSigner: ResourceUrlSigner,
) : AbstractFileResourceService(storageProvider, fileResourceService) {
    private val logger = logger()

    private val baseDirectory: Path by lazy {
        Paths.get(basePath).toAbsolutePath().normalize()
    }

    init {
        val dir = baseDirectory.toFile()
        if (!dir.exists()) {
            dir.mkdirs()
            logger.info("Created local file storage directory: ${dir.canonicalPath}")
        }
    }

    override suspend fun doUploadFile(
        fileType: ResourceFileType,
        fileLength: Long,
        fileContentType: String,
        fileNameWithExtension: String,
        inputStream: InputStream,
        objectKey: String,
        progressReporter: ((Int) -> Unit)?
    ): Exception? {
        return try {
            val targetPath = getFilePath(objectKey)
            val parentDir = targetPath.parent?.toFile()

            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs()
            }

            withContext(Dispatchers.IO) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING)
            }

            logger.info("File uploaded successfully to local storage: $targetPath")
            null
        } catch (e: Exception) {
            logger.error("An error occurred while uploading file to local storage", e)
            e
        }
    }

    fun getFile(objectKey: String): File? {
        val targetPath = resolvePath(objectKey)
        val file = targetPath.toFile()

        return if (file.exists() && file.isFile) {
            file
        } else {
            null
        }
    }

    fun getFilePath(objectKey: String): Path {
        return resolvePath(objectKey)
    }

    private fun resolvePath(objectKey: String): Path {
        val normalizedObjectKey = objectKey.removePrefix("/")
        val resolved = baseDirectory.resolve(normalizedObjectKey).normalize()
        if (!resolved.startsWith(baseDirectory)) {
            throw BusinessException("Invalid object key")
        }
        return resolved
    }

    /**
     * Local files are served by [com.lovelycatv.crystalframework.resource.controller.LocalFileResourceController].
     * Public files use a stable, cacheable URL; non-public files get a short-lived HMAC signature so an
     * anonymous `<img>` request (which cannot carry an Authorization header) can be verified at read time.
     */
    override suspend fun buildDownloadUrl(entity: FileResourceEntity, visibility: ResourceVisibility): String {
        val systemSettings = systemModuleClient.getSystemSettings()
            ?: throw BusinessException("System settings not initialized")
        val baseUrl = systemSettings.basic.getNormalizedBaseUrl(false)

        return when (visibility) {
            ResourceVisibility.PUBLIC ->
                "$baseUrl/file/local/${entity.id}"
            ResourceVisibility.AUTHENTICATED,
            ResourceVisibility.SCOPE_MEMBER,
            ResourceVisibility.OWNER_ONLY,
            ResourceVisibility.SYSTEM_ADMIN -> {
                val expiresAt = System.currentTimeMillis() + LOCAL_SIGNED_URL_TTL_MS
                val signature = resourceUrlSigner.sign(entity.id, expiresAt)
                "$baseUrl/file/local/${entity.id}?exp=$expiresAt&sig=$signature"
            }
        }
    }

    companion object {
        /** Validity window of a signed local-file download URL (5 minutes). */
        private const val LOCAL_SIGNED_URL_TTL_MS = 5 * 60 * 1000L
    }
}
