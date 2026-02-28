package com.lovelycatv.crystalframework.resource.service.api.impl

import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.service.FileResourceService
import com.lovelycatv.crystalframework.resource.service.api.AbstractFileResourceService
import com.lovelycatv.crystalframework.resource.types.ResourceFileType
import com.lovelycatv.crystalframework.resource.types.StorageProviderType
import com.lovelycatv.vertex.log.logger
import com.qcloud.cos.COSClient
import com.qcloud.cos.ClientConfig
import com.qcloud.cos.auth.BasicCOSCredentials
import com.qcloud.cos.model.PutObjectRequest
import com.qcloud.cos.region.Region
import com.qcloud.cos.transfer.TransferManager
import com.qcloud.cos.transfer.TransferManagerConfiguration
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.util.concurrent.Executors
import kotlin.coroutines.resume

class COSFileResourceServiceImpl(
    storageProvider: StorageProviderEntity,
    fileResourceService: FileResourceService,
    private val accessKey: String,
    private val secretKey: String,
    private val region: String,
    private val bucketName: String,
) : AbstractFileResourceService(storageProvider, fileResourceService) {
    private val logger = logger()

    private var client: COSClient? = null
    private var transferManager: TransferManager? = null

    private fun getClient(): COSClient {
        if (this.client == null) {
            // 1 (secretId, secretKey)
            val cred = BasicCOSCredentials(accessKey, secretKey)

            // 2 https://cloud.tencent.com/document/product/436/6224
            val clientConfig = ClientConfig(Region(region))

            this.client = COSClient(cred, clientConfig)
        }

        return this.client!!
    }

    private fun getTransferManager(): TransferManager {
        if (this.transferManager == null) {
            val cosClient = getClient()

            val threadPool = Executors.newFixedThreadPool(16)

            this.transferManager = TransferManager(cosClient, threadPool).apply {
                val transferManagerConfiguration = TransferManagerConfiguration()
                transferManagerConfiguration.multipartUploadThreshold = 5 * 1024 *1024
                transferManagerConfiguration.minimumUploadPartSize = 1 * 1024 *1024

                configuration = transferManagerConfiguration
            }
        }

        return this.transferManager!!
    }

    override suspend fun doUploadFile(
        fileType: ResourceFileType,
        file: File,
        objectKey: String,
        progressReporter: ((Int) -> Unit)?
    ): Boolean {
        val request = PutObjectRequest(bucketName, objectKey, file)

        return suspendCancellableCoroutine { continuation ->
            try {
                val upload = getTransferManager()
                    .upload(request)

                progressReporter?.let { reporter ->
                    upload.addProgressListener {
                        reporter.invoke(((it.bytesTransferred.toDouble() / it.bytes) * 100).toInt())
                    }
                }

                upload.waitForCompletion()

                continuation.resume(true)
            } catch (e: Exception) {
                logger.error("An error occurred while uploading file to COS", e)
                continuation.resume(false)
            }
        }
    }

    override fun destroy() {
        super.destroy()

        this.transferManager?.shutdownNow(true)
    }
}