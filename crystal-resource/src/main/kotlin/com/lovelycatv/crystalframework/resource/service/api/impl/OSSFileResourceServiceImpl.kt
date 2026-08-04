package com.lovelycatv.crystalframework.resource.service.api.impl

import com.aliyun.sdk.service.oss2.OSSClient
import com.aliyun.sdk.service.oss2.PresignOptions
import com.aliyun.sdk.service.oss2.credentials.StaticCredentialsProvider
import com.aliyun.sdk.service.oss2.models.GetObjectRequest
import com.aliyun.sdk.service.oss2.models.PutObjectRequest
import com.aliyun.sdk.service.oss2.transport.BinaryData
import com.lovelycatv.crystalframework.resource.entity.FileResourceEntity
import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.service.FileResourceService
import com.lovelycatv.crystalframework.resource.service.api.AbstractFileResourceService
import com.lovelycatv.crystalframework.resource.types.ResourceFileType
import com.lovelycatv.crystalframework.shared.types.common.ResourceVisibility
import com.lovelycatv.vertex.log.logger
import java.io.InputStream
import java.time.Duration

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

    override suspend fun buildDownloadUrl(entity: FileResourceEntity, visibility: ResourceVisibility): String {
        val objectKey = normalizedObjectKey(entity)

        return when (visibility) {
            ResourceVisibility.PUBLIC ->
                "${normalizedProviderBaseUrl()}$objectKey"
            ResourceVisibility.AUTHENTICATED,
            ResourceVisibility.SCOPE_MEMBER,
            ResourceVisibility.OWNER_ONLY,
            ResourceVisibility.SYSTEM_ADMIN ->
                getClient().presign(
                    GetObjectRequest.newBuilder()
                        .bucket(bucketName)
                        .key(objectKey)
                        .build(),
                    PresignOptions.newBuilder()
                        .expiration(Duration.ofMillis(PRESIGNED_URL_TTL_MS))
                        .build()
                ).url()
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

    companion object {
        /** TTL for pre-signed GET URLs of non-public resources (30 minutes). */
        private const val PRESIGNED_URL_TTL_MS = 30 * 60 * 1000L
    }
}