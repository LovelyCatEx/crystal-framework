package com.lovelycatv.crystalframework.resource.service.api

import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.interfaces.RoutingContext
import com.lovelycatv.crystalframework.resource.interfaces.StorageProviderRouter
import com.lovelycatv.crystalframework.resource.service.api.factory.FileResourceServiceFactory
import com.lovelycatv.crystalframework.resource.service.api.result.FileUploadResult
import com.lovelycatv.crystalframework.resource.utils.detectMimeType
import com.lovelycatv.crystalframework.sdk.resource.file.types.ResourceFileTypeDeclaration
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.types.common.ResourceScope
import com.lovelycatv.crystalframework.shared.utils.asInputStreamWithLength
import org.springframework.beans.factory.getBeansOfType
import org.springframework.context.ApplicationContext
import org.springframework.core.OrderComparator
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.util.concurrent.ConcurrentHashMap
import jakarta.annotation.PreDestroy

@Component
class FileResourceServiceManager(
    private val applicationContext: ApplicationContext
) {
    private val cacheMap = ConcurrentHashMap<Long, AbstractFileResourceService>()
    private val cacheLock = Any()

    suspend fun getService(context: RoutingContext): AbstractFileResourceService {
        val routers = applicationContext
            .getBeansOfType<StorageProviderRouter>()
            .values

        val storageProvider = routers.minWithOrNull(OrderComparator.INSTANCE)
            ?.get(context)
            ?: throw BusinessException("No route found for file resource service")

        return this.getService(storageProvider)
    }

    /**
     * One-stop upload: reads [file] bytes, detects actual MIME type via magic numbers,
     * routes to the appropriate storage provider, and delegates to [AbstractFileResourceService.uploadFile].
     * Callers never need to repeat the read → detect → route → upload sequence manually.
     */
    suspend fun uploadFile(
        userId: Long,
        scope: ResourceScope,
        scopeId: Long,
        fileType: ResourceFileTypeDeclaration,
        file: FilePart,
        targetFileName: String,
        progressReporter: ((Int) -> Unit)? = null
    ): FileUploadResult {
        val (rawStream, fileSize) = file.asInputStreamWithLength()
        val fileBytes = rawStream.readBytes()
        val detectedMimeType = detectMimeType(fileBytes)

        val service = getService(
            RoutingContext.of(
                userId = userId,
                fileType = fileType,
                fileName = targetFileName,
                fileContentType = detectedMimeType,
                fileSize = fileSize,
            )
        )

        return service.uploadFile(
            userId, scope, scopeId, fileType, targetFileName,
            fileSize,
            ByteArrayInputStream(fileBytes),
            progressReporter
        )
    }

    fun getService(provider: StorageProviderEntity): AbstractFileResourceService {
        synchronized(cacheLock) {
            return cacheMap.computeIfAbsent(provider.id) {
                val serviceFactories = applicationContext
                    .getBeansOfType<FileResourceServiceFactory<*>>()
                    .values

                val factory = serviceFactories
                    .filter { it.getStorageProviderTypeId() == provider.type }
                    .minWithOrNull(OrderComparator.INSTANCE)
                    ?: throw BusinessException("No file resource service factory found for provider ${provider.id}")

                factory.build(provider)
            }
        }
    }

    fun invalidateService(providerId: Long) {
        synchronized(cacheLock) {
            cacheMap.remove(providerId)?.destroy()
        }
    }

    @PreDestroy
    fun destroy() {
        synchronized(cacheLock) {
            cacheMap.values.forEach { it.destroy() }
            cacheMap.clear()
        }
    }
}