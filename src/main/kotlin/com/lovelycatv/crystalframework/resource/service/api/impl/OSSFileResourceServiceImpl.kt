package com.lovelycatv.crystalframework.resource.service.api.impl

import com.aliyun.sdk.service.oss2.OSSClient
import com.aliyun.sdk.service.oss2.credentials.StaticCredentialsProvider
import com.aliyun.sdk.service.oss2.models.PutObjectRequest
import com.aliyun.sdk.service.oss2.transport.BinaryData
import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.service.FileResourceService
import com.lovelycatv.crystalframework.resource.service.api.AbstractFileResourceService
import com.lovelycatv.crystalframework.resource.types.ResourceFileType
import com.lovelycatv.vertex.log.logger
import java.io.InputStream

class OSSFileResourceServiceImpl(
    storageProvider: StorageProviderEntity,
    fileResourceService: FileResourceService,
    private val accessKeyId: String,
    private val accessKeySecret: String,
    private val securityToken: String,
    private val region: String,
    private val bucketName: String,
) : AbstractFileResourceService(storageProvider, fileResourceService) {
    private val logger = logger()

    private var client: OSSClient? = null

    fun getClient(): OSSClient {
        if (client == null) {
            val clientBuilder = OSSClient.newBuilder()
                .credentialsProvider(
                    StaticCredentialsProvider(
                        this.accessKeyId,
                        this.accessKeySecret,
                        this.securityToken,
                    )
                )
                .region(region)

            this.client = clientBuilder.build()
        }

        return this.client!!
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
            getClient().putObject(
                PutObjectRequest
                    .newBuilder()
                    .bucket(this.bucketName)
                    .key(objectKey)
                    .body(BinaryData.fromStream(inputStream))
                    .build()
            )

            null
        } catch (e: Exception) {
            logger.error("An error occurred while uploading file to OSS", e)
            e
        }
    }

    override fun destroy() {
        super.destroy()

        this.client?.close()
    }
}