package com.lovelycatv.crystalframework.resource.service.impl

import com.lovelycatv.crystalframework.shared.config.CrystalFrameworkConfiguration
import com.lovelycatv.crystalframework.resource.entity.FileResourceEntity
import com.lovelycatv.crystalframework.resource.repository.FileResourceRepository
import com.lovelycatv.crystalframework.resource.service.FileResourceService
import com.lovelycatv.crystalframework.resource.service.ResourceAccessService
import com.lovelycatv.crystalframework.resource.service.StorageProviderService
import com.lovelycatv.crystalframework.resource.utils.ResourceUrlSigner
import com.lovelycatv.crystalframework.resource.types.ResourceFileType
import com.lovelycatv.crystalframework.resource.types.StorageProviderType
import com.lovelycatv.crystalframework.shared.api.system.SystemModuleClient
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.types.common.ResourceVisibility
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
    private val reactiveRedisService: ReactiveRedisService,
    override val eventPublisher: ApplicationEventPublisher,
    private val systemModuleClient: SystemModuleClient,
    private val resourceAccessService: ResourceAccessService,
    private val resourceUrlSigner: ResourceUrlSigner,
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
            ResourceFileType.TENANT_MEMBER_AVATAR -> resourceConfig.tenantMemberAvatar
        }
        return contentType in config.supportedContentTypes
    }

    override suspend fun getByMD5(md5: String): FileResourceEntity? {
        return this.getRepository()
            .findByMd5(md5)
            .awaitFirstOrNull()
    }

    override suspend fun getFileDownloadUrl(entity: FileResourceEntity, viewer: UserAuthentication?): String {
        // Mint-time access check: never hand out a URL the viewer is not entitled to read.
        resourceAccessService.assertReadable(entity, viewer)

        val provider = storageProviderService
            .getByIdOrThrow(entity.storageProviderId)

        if (provider.getRealStorageProviderType() == StorageProviderType.LOCAL_FILE_SYSTEM) {
            // This path is related to LocalFileResourceController$readLocalFile
            val systemSettings = systemModuleClient.getSystemSettings()
                ?: throw BusinessException("System settings not initialized")
            val baseUrl = systemSettings.basic.getNormalizedBaseUrl(false)

            // Public local files are served by a stable, cacheable URL; anything else gets a
            // short-lived HMAC signature so an anonymous <img> request can be verified at read time.
            return if (resourceAccessService.resolveVisibility(entity.getRealResourceFileType()) == ResourceVisibility.PUBLIC) {
                "$baseUrl/file/local/${entity.id}"
            } else {
                val expiresAt = System.currentTimeMillis() + LOCAL_SIGNED_URL_TTL_MS
                val signature = resourceUrlSigner.sign(entity.id, expiresAt)
                "$baseUrl/file/local/${entity.id}?exp=$expiresAt&sig=$signature"
            }
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

    override val cacheStore: ReactiveExpiringKVStore<String, FileResourceEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<FileResourceEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<FileResourceEntity> = FileResourceEntity::class

    companion object {
        /** Validity window of a signed local-file download URL (5 minutes). */
        private const val LOCAL_SIGNED_URL_TTL_MS = 5 * 60 * 1000L
    }
}