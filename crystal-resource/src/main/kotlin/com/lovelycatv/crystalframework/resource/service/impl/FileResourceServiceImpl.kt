package com.lovelycatv.crystalframework.resource.service.impl

import com.lovelycatv.crystalframework.shared.config.CrystalFrameworkConfiguration
import com.lovelycatv.crystalframework.resource.entity.FileResourceEntity
import com.lovelycatv.crystalframework.resource.repository.FileResourceRepository
import com.lovelycatv.crystalframework.resource.service.FileResourceService
import com.lovelycatv.crystalframework.resource.service.StorageProviderService
import com.lovelycatv.crystalframework.resource.types.ResourceFileType
import com.lovelycatv.crystalframework.resource.types.StorageProviderType
import com.lovelycatv.crystalframework.shared.api.system.SystemModuleClient
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import kotlin.reflect.KClass

@Service
class FileResourceServiceImpl(
    private val fileResourceRepository: FileResourceRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val storageProviderService: StorageProviderService,
    private val crystalFrameworkConfiguration: CrystalFrameworkConfiguration,
    private val redisService: RedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val systemModuleClient: SystemModuleClient,
) : FileResourceService {
    override fun getRepository(): FileResourceRepository {
        return this.fileResourceRepository
    }

    override fun generateNextSnowId(gene: Long): Long {
        return snowIdGenerator.nextId(gene)
    }

    override fun getCrystalFrameworkConfiguration(): CrystalFrameworkConfiguration {
        return this.crystalFrameworkConfiguration
    }

    override fun checkFileContentType(
        fileType: ResourceFileType,
        contentType: String
    ): Boolean {
        val resourceConfig = this.getCrystalFrameworkConfiguration().resource
        val config = when (fileType) {
            ResourceFileType.USER_AVATAR -> resourceConfig.avatar
            ResourceFileType.TENANT_ICON -> resourceConfig.tenantIcon
        }
        return contentType in config.supportedContentTypes
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
            val systemSettings = systemModuleClient.getSystemSettings()
                ?: throw BusinessException("System settings not initialized")
            return "${systemSettings.basic.getNormalizedBaseUrl(false)}/file/local/${entity.id}"
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
    override val entityClass: KClass<FileResourceEntity> = FileResourceEntity::class
}