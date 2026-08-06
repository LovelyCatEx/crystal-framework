package com.lovelycatv.crystalframework.resource.service.api.impl

import com.aliyun.sdk.service.oss2.OSSClient
import com.aliyun.sdk.service.oss2.PresignOptions
import com.aliyun.sdk.service.oss2.credentials.StaticCredentialsProvider
import com.aliyun.sdk.service.oss2.models.AbortMultipartUploadRequest
import com.aliyun.sdk.service.oss2.models.CompleteMultipartUpload
import com.aliyun.sdk.service.oss2.models.CompleteMultipartUploadRequest
import com.aliyun.sdk.service.oss2.models.GetObjectRequest
import com.aliyun.sdk.service.oss2.models.InitiateMultipartUploadRequest
import com.aliyun.sdk.service.oss2.models.Part
import com.aliyun.sdk.service.oss2.models.PutObjectRequest
import com.aliyun.sdk.service.oss2.models.UploadPartRequest
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
    basePath: String,
) : AbstractFileResourceService(storageProvider, fileResourceService, basePath) {
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

    override suspend fun buildPublicDownloadUrl(entity: FileResourceEntity): String {
        return "${normalizedProviderBaseUrl()}${normalizedObjectKey(entity)}"
    }

    override suspend fun buildSignedDownloadUrl(entity: FileResourceEntity, signedUrlTtlSeconds: Long): String {
        return getClient().presign(
            GetObjectRequest.newBuilder()
                .bucket(bucketName)
                .key(normalizedObjectKey(entity))
                .build(),
            PresignOptions.newBuilder()
                .expiration(Duration.ofSeconds(signedUrlTtlSeconds))
                .build()
        ).url()
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
        val ossClient = getClient()
        val uploadId = try {
            ossClient.initiateMultipartUpload(
                InitiateMultipartUploadRequest.newBuilder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(fileContentType)
                    .build()
            ).initiateMultipartUpload().uploadId()
        } catch (e: Exception) {
            logger.error("An error occurred while initializing OSS multipart upload", e)
            return e
        }

        return try {
            val uploadedParts = mutableListOf<Part>()
            var uploadedBytes = 0L
            var partNumber = 1L

            while (true) {
                val partBytes = inputStream.readNBytes(PART_SIZE)
                if (partBytes.isEmpty()) {
                    break
                }

                val uploadPartResult = ossClient.uploadPart(
                    UploadPartRequest.newBuilder()
                        .bucket(bucketName)
                        .key(objectKey)
                        .uploadId(uploadId)
                        .partNumber(partNumber)
                        .contentLength(partBytes.size.toLong())
                        .body(BinaryData.fromBytes(partBytes))
                        .build()
                )

                uploadedParts.add(
                    Part.newBuilder()
                        .partNumber(partNumber)
                        .eTag(uploadPartResult.eTag())
                        .build()
                )

                uploadedBytes += partBytes.size
                progressReporter?.invoke(computeProgress(uploadedBytes, fileLength))
                partNumber++
            }

            ossClient.completeMultipartUpload(
                CompleteMultipartUploadRequest.newBuilder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .uploadId(uploadId)
                    .completeMultipartUpload(
                        CompleteMultipartUpload.newBuilder()
                            .parts(uploadedParts)
                            .build()
                    )
                    .build()
            )

            null
        } catch (e: Exception) {
            logger.error("An error occurred while uploading file to OSS", e)
            abortMultipartUploadQuietly(ossClient, objectKey, uploadId)
            e
        }
    }

    private suspend fun putObject(
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
                PutObjectRequest.newBuilder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentLength(fileLength.toInt())
                    .contentType(fileContentType)
                    .body(BinaryData.fromStream(inputStream))
                    .build()
            )

            progressReporter?.invoke(PROGRESS_COMPLETE)
            null
        } catch (e: Exception) {
            logger.error("An error occurred while uploading file to OSS with putObject", e)
            e
        }
    }

    private fun abortMultipartUploadQuietly(ossClient: OSSClient, objectKey: String, uploadId: String) {
        try {
            ossClient.abortMultipartUpload(
                AbortMultipartUploadRequest.newBuilder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .uploadId(uploadId)
                    .build()
            )
        } catch (e: Exception) {
            logger.error("Failed to abort OSS multipart upload, uploadId=$uploadId", e)
        }
    }

    private fun computeProgress(uploadedBytes: Long, fileLength: Long): Int {
        if (fileLength <= 0L) {
            return 0
        }

        return ((uploadedBytes.toDouble() / fileLength) * PROGRESS_COMPLETE)
            .toInt()
            .coerceIn(0, PROGRESS_COMPLETE)
    }

    override fun destroy() {
        super.destroy()

        this.client?.close()
    }

    companion object {
        private const val PART_SIZE = 5 * 1024 * 1024

        private const val PROGRESS_COMPLETE = 100
    }
}
