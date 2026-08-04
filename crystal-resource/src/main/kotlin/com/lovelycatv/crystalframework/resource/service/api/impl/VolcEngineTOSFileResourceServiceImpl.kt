package com.lovelycatv.crystalframework.resource.service.api.impl

import com.lovelycatv.crystalframework.resource.entity.FileResourceEntity
import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.service.FileResourceService
import com.lovelycatv.crystalframework.resource.service.api.AbstractFileResourceService
import com.lovelycatv.crystalframework.resource.types.ResourceFileType
import com.lovelycatv.crystalframework.shared.types.common.ResourceVisibility
import com.lovelycatv.vertex.log.logger
import com.volcengine.tos.TOSV2
import com.volcengine.tos.TOSV2ClientBuilder
import com.volcengine.tos.comm.HttpMethod
import com.volcengine.tos.model.`object`.AbortMultipartUploadInput
import com.volcengine.tos.model.`object`.CompleteMultipartUploadV2Input
import com.volcengine.tos.model.`object`.CreateMultipartUploadInput
import com.volcengine.tos.model.`object`.ObjectMetaRequestOptions
import com.volcengine.tos.model.`object`.PreSignedURLInput
import com.volcengine.tos.model.`object`.UploadPartV2Input
import com.volcengine.tos.model.`object`.UploadedPartV2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.InputStream

class VolcEngineTOSFileResourceServiceImpl(
    storageProvider: StorageProviderEntity,
    fileResourceService: FileResourceService,
    private val accessKey: String,
    private val secretKey: String,
    private val region: String,
    private val endpoint: String,
    private val bucketName: String,
    basePath: String,
) : AbstractFileResourceService(storageProvider, fileResourceService, basePath) {
    private val logger = logger()

    private var client: TOSV2? = null

    private fun getClient(): TOSV2 {
        if (client == null) {
            this.client = TOSV2ClientBuilder().build(region, endpoint, accessKey, secretKey)
        }

        return this.client!!
    }

    override suspend fun buildPublicDownloadUrl(entity: FileResourceEntity): String {
        return "${normalizedProviderBaseUrl()}${normalizedObjectKey(entity)}"
    }

    override suspend fun buildSignedDownloadUrl(entity: FileResourceEntity, signedUrlTtlSeconds: Long): String {
        return withContext(Dispatchers.IO) {
            getClient().preSignedURL(
                PreSignedURLInput.builder()
                    .httpMethod(HttpMethod.GET)
                    .bucket(bucketName)
                    .key(normalizedObjectKey(entity))
                    .expires(signedUrlTtlSeconds)
                    .build()
            ).signedUrl
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
        val tos = getClient()

        // 1. Initialize the multipart upload, carrying content type and disposition as object metadata.
        val createOutput = tos.createMultipartUpload(
            CreateMultipartUploadInput()
                .setBucket(bucketName)
                .setKey(objectKey)
                .setOptions(
                    ObjectMetaRequestOptions()
                        .setContentType(fileContentType)
                        .setContentDisposition("${CONTENT_DISPOSITION_PREFIX}\"$fileNameWithExtension\"")
                )
        )
        val uploadId: String = createOutput.uploadID

        return try {
            // 2. Sequentially read the non-seekable stream part by part and upload each part.
            val uploadedParts = mutableListOf<UploadedPartV2>()
            var uploadedBytes = 0L
            var partNumber = 1

            while (true) {
                val partBytes = withContext(Dispatchers.IO) {
                    inputStream.readNBytes(PART_SIZE)
                }
                if (partBytes.isEmpty()) {
                    break
                }

                val partOutput = tos.uploadPart(
                    UploadPartV2Input()
                        .setBucket(bucketName)
                        .setKey(objectKey)
                        .setUploadID(uploadId)
                        .setPartNumber(partNumber)
                        .setContentLength(partBytes.size.toLong())
                        .setContent(ByteArrayInputStream(partBytes))
                )

                uploadedParts.add(
                    UploadedPartV2()
                        .setPartNumber(partNumber)
                        .setEtag(partOutput.etag)
                )

                uploadedBytes += partBytes.size
                progressReporter?.invoke(computeProgress(uploadedBytes, fileLength))
                partNumber++
            }

            // 3. Merge the uploaded parts into the final object.
            tos.completeMultipartUpload(
                CompleteMultipartUploadV2Input()
                    .setBucket(bucketName)
                    .setKey(objectKey)
                    .setUploadID(uploadId)
                    .setUploadedParts(uploadedParts)
            )

            null
        } catch (e: Exception) {
            logger.error("An error occurred while uploading file to VolcEngine TOS", e)
            abortQuietly(tos, objectKey, uploadId)
            e
        }
    }

    private fun abortQuietly(tos: TOSV2, objectKey: String, uploadId: String) {
        try {
            tos.abortMultipartUpload(
                AbortMultipartUploadInput()
                    .setBucket(bucketName)
                    .setKey(objectKey)
                    .setUploadID(uploadId)
            )
        } catch (e: Exception) {
            logger.error("Failed to abort VolcEngine TOS multipart upload, uploadId=$uploadId", e)
        }
    }

    private fun computeProgress(uploadedBytes: Long, fileLength: Long): Int {
        if (fileLength <= 0L) {
            return 0
        }

        return ((uploadedBytes.toDouble() / fileLength) * PROGRESS_COMPLETE).toInt().coerceIn(0, PROGRESS_COMPLETE)
    }

    override fun destroy() {
        super.destroy()

        this.client?.close()
    }

    companion object {
        /** TOS requires every part except the last to be at least 5 MB. */
        private const val PART_SIZE = 5 * 1024 * 1024

        private const val PROGRESS_COMPLETE = 100

        private const val CONTENT_DISPOSITION_PREFIX = "attachment; filename="
    }
}
