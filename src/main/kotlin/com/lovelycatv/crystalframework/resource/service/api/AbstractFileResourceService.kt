package com.lovelycatv.crystalframework.resource.service.api

import com.lovelycatv.crystalframework.resource.entity.FileResourceEntity
import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.service.FileResourceService
import com.lovelycatv.crystalframework.resource.service.api.result.FileUploadResult
import com.lovelycatv.crystalframework.resource.types.ResourceFileType
import com.lovelycatv.crystalframework.resource.types.StorageProviderType
import com.lovelycatv.crystalframework.shared.utils.FileMD5Utils
import com.lovelycatv.crystalframework.shared.utils.asInputStreamWithLength
import com.lovelycatv.crystalframework.shared.utils.getContentType
import com.lovelycatv.crystalframework.shared.utils.toJSONString
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.http.codec.multipart.FilePart
import java.io.ByteArrayInputStream
import java.io.InputStream

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

    suspend fun uploadFile(
        userId: Long,
        fileType: ResourceFileType,
        filePart: FilePart,
        targetFileName: String,
        progressReporter: ((Int) -> Unit)? = null
    ): FileUploadResult {
        val (inputStream, fileSize) = filePart.asInputStreamWithLength()

        return this.uploadFile(
            userId = userId,
            fileType = fileType,
            fileNameWithExtension = targetFileName,
            fileLength = fileSize,
            fileContentType = filePart.getContentType(),
            inputStream = inputStream,
            progressReporter = progressReporter
        )
    }

    suspend fun uploadFile(
        userId: Long,
        fileType: ResourceFileType,
        fileNameWithExtension: String,
        fileLength: Long,
        fileContentType: String,
        inputStream: InputStream,
        progressReporter: ((Int) -> Unit)? = null
    ): FileUploadResult {
        fileResourceService.assertFileContentType(
            fileType,
            fileContentType
        )

        val byteArray = inputStream.readBytes()

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

        val objectKey = this.buildObjectKey(fileType, fileNameWithExtension)

        val (fileName, fileExtension) = fileNameWithExtension.run {
            this.split(".")
        }

        val result = this.doUploadFile(
            fileType,
            fileLength,
            fileContentType,
            fileNameWithExtension,
            uploadStream,
            objectKey,
            progressReporter
        )

        return if (result == null) {
            val fileResourceEntity = fileResourceService.getRepository().save(
                FileResourceEntity(
                    id = fileResourceService.generateNextSnowId(),
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

    open fun destroy() {}
}