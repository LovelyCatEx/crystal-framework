package com.lovelycatv.crystalframework.resource.service.impl

import com.lovelycatv.crystalframework.resource.config.ResourceModuleConfiguration
import com.lovelycatv.crystalframework.resource.entity.FileResourceEntity
import com.lovelycatv.crystalframework.resource.repository.FileResourceRepository
import com.lovelycatv.crystalframework.resource.service.FileResourceService
import com.lovelycatv.crystalframework.resource.service.StorageProviderService
import com.lovelycatv.crystalframework.resource.types.ResourceFileType
import com.lovelycatv.crystalframework.resource.types.StorageProviderType
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.system.service.SystemSettingsService
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service

@Service
class FileResourceServiceImpl(
    private val fileResourceRepository: FileResourceRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val storageProviderService: StorageProviderService,
    private val resourceModuleConfiguration: ResourceModuleConfiguration,
    private val systemSettingsService: SystemSettingsService,
    private val redisService: RedisService
) : FileResourceService {
    override fun getRepository(): FileResourceRepository {
        return this.fileResourceRepository
    }

    override fun generateNextSnowId(gene: Long): Long {
        return snowIdGenerator.nextId(gene)
    }

    override fun getResourceModuleConfiguration(): ResourceModuleConfiguration {
        return this.resourceModuleConfiguration
    }

    override fun checkFileContentType(
        fileType: ResourceFileType,
        contentType: String
    ): Boolean {
        return contentType in this.getResourceModuleConfiguration()
            .get(fileType)
            .supportedContentTypes
    }

    override suspend fun getByMD5(md5: String): FileResourceEntity? {
        return this.getRepository()
            .findByMd5(md5)
            .awaitFirstOrNull()
    }

    override suspend fun getFileDownloadUrl(entity: FileResourceEntity): String {
        val provider = storageProviderService
            .getByIdOrThrow(entity.storageProviderId)

        if (provider.getRealStorageProviderType() == StorageProviderType.LOCAL_FILE_SYSTEM) {
            // This path is related to LocalFileResourceController$readLocalFile
            return "${systemSettingsService.getSystemSettings().basic.getNormalizedBaseUrl(false)}/file/local/${entity.id}"
        }

        val baseUrl = provider.baseUrl
            .run {
                if (this.endsWith("/")) {
                    this
                } else {
                    "$this/"
                }
            }

        val key = entity.objectKey.run {
            if (this.startsWith("/")) {
                this.replaceFirst("/", "")
            } else {
                this
            }
        }

        return baseUrl + key
    }

    override val cacheStore: ExpiringKVStore<String, FileResourceEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<FileResourceEntity>>
        get() = redisService.asKVStore()
}