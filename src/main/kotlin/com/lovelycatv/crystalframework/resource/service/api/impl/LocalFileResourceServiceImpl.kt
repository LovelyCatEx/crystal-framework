package com.lovelycatv.crystalframework.resource.service.api.impl

import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.service.FileResourceService
import com.lovelycatv.crystalframework.resource.service.api.AbstractFileResourceService
import com.lovelycatv.crystalframework.resource.types.ResourceFileType
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
        return baseDirectory.resolve(normalizedObjectKey).normalize()
    }

    override fun destroy() {
        super.destroy()
    }
}
