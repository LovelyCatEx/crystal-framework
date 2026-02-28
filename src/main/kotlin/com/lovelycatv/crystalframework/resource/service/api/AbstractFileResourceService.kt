package com.lovelycatv.crystalframework.resource.service.api

import com.lovelycatv.crystalframework.resource.entity.FileResourceEntity
import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.service.FileResourceService
import com.lovelycatv.crystalframework.resource.service.api.result.FileUploadResult
import com.lovelycatv.crystalframework.resource.types.ResourceFileType
import com.lovelycatv.crystalframework.resource.types.StorageProviderType
import com.lovelycatv.crystalframework.shared.utils.FileMD5Utils
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.reactive.awaitFirstOrNull
import java.io.File

abstract class AbstractFileResourceService(
    private val storageProvider: StorageProviderEntity,
    private val fileResourceService: FileResourceService
) {
    private val logger = logger()

    fun getStorageProvider(): StorageProviderType {
        return this.storageProvider.getRealStorageProviderType()
    }

    open fun buildObjectKey(fileType: ResourceFileType, fileNameWithExtension: String): String {
        return "/${fileType.name.lowercase()}/$fileNameWithExtension"
    }

    suspend fun uploadFile(
        userId: Long,
        fileType: ResourceFileType,
        file: File,
        progressReporter: ((Int) -> Unit)? = null
    ): FileUploadResult {
        val md5 = FileMD5Utils.calculateMD5(file)

        val existing = fileResourceService.getByMD5(md5)
        if (existing != null) {
            logger.info("File ${file.name} already exists with md5 $md5, upload skipped")

            return FileUploadResult(
                success = true,
                providerType = getStorageProvider(),
                fileType = fileType,
                objectKey = existing.objectKey,
                fileResourceEntity = existing
            )
        }

        val objectKey = this.buildObjectKey(fileType, file.name).run {
            if (!this.startsWith("/")) {
                "/$this"
            } else {
                this
            }
        }

        val (fileName, fileExtension) = file.name.run {
            this.split(".")
        }

        val result = this.doUploadFile(fileType, file, objectKey, progressReporter)

        return if (result) {
            val fileResourceEntity = fileResourceService.getRepository().save(
                FileResourceEntity(
                    id = fileResourceService.generateNextSnowId(),
                    userId = userId,
                    type = fileType.typeId,
                    fileName = fileName,
                    fileExtension = fileExtension,
                    md5 = md5,
                    fileSize = file.length(),
                    storageProviderId = storageProvider.id,
                    objectKey = objectKey
                ) newEntity true
            ).awaitFirstOrNull()

            FileUploadResult(
                success = true,
                providerType = getStorageProvider(),
                fileType = fileType,
                objectKey = objectKey,
                fileResourceEntity = fileResourceEntity
            )
        } else {
            FileUploadResult(
                success = false,
                providerType = getStorageProvider(),
                fileType = fileType,
                objectKey = objectKey,
                fileResourceEntity = null
            )
        }
    }

    abstract suspend fun doUploadFile(
        fileType: ResourceFileType,
        file: File,
        objectKey: String,
        progressReporter: ((Int) -> Unit)? = null
    ): Boolean

    open fun destroy() {}
}