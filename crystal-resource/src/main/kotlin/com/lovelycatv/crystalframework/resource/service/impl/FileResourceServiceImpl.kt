package com.lovelycatv.crystalframework.resource.service.impl

import com.lovelycatv.crystalframework.shared.config.CrystalFrameworkConfiguration
import com.lovelycatv.crystalframework.resource.entity.FileResourceEntity
import com.lovelycatv.crystalframework.resource.repository.FileResourceRepository
import com.lovelycatv.crystalframework.resource.service.FileResourceService
import com.lovelycatv.crystalframework.resource.service.ResourceAccessService
import com.lovelycatv.crystalframework.resource.service.StorageProviderService
import com.lovelycatv.crystalframework.resource.service.api.FileResourceServiceManager
import com.lovelycatv.crystalframework.resource.types.FileResourceStatus
import com.lovelycatv.crystalframework.resource.types.ResourceFileType
import com.lovelycatv.crystalframework.resource.utils.getMimeExtensions
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
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
    private val resourceAccessService: ResourceAccessService,
    private val fileResourceServiceManager: FileResourceServiceManager,
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

    override fun resolveFileExtension(
        fileType: ResourceFileType,
        contentType: String,
    ): String? {
        val resourceConfig = this.getCrystalFrameworkConfiguration().resource
        val config = when (fileType) {
            ResourceFileType.USER_AVATAR -> resourceConfig.avatar
            ResourceFileType.TENANT_ICON -> resourceConfig.tenantIcon
            ResourceFileType.TENANT_MEMBER_AVATAR -> resourceConfig.tenantMemberAvatar
        }
        val allowedExtensions = config.supportedFileExtensions
            .map { it.removePrefix(".").lowercase() }
            .toSet()
        return getMimeExtensions(contentType).firstOrNull { it in allowedExtensions }
    }

    override suspend fun getCommitedFileByMD5(md5: String, scope: Int, scopeId: Long): FileResourceEntity? {
        return fileResourceRepository.findByMd5AndScopeAndScopeIdAndStatus(
            md5,
            scope,
            scopeId,
            FileResourceStatus.COMMITTED.typeId,
        ).awaitFirstOrNull()
    }

    override suspend fun reservePreUpload(entity: FileResourceEntity): FileResourceEntity? {
        return withInvalidateEntityCacheContext(entity.id) {
            fileResourceRepository.save(entity newEntity true).awaitFirstOrNull()
        }
    }

    override suspend fun commitUpload(entity: FileResourceEntity): Boolean {
        return withInvalidateEntityCacheContext(entity.id) {
            fileResourceRepository.commitUpload(
                entity.id,
                requireNotNull(entity.uploadToken),
                FileResourceStatus.UPLOADING.typeId,
                FileResourceStatus.COMMITTED.typeId,
            ).awaitFirstOrNull() == 1L
        }
    }

    override suspend fun removeUploadRecord(entity: FileResourceEntity): Boolean {
        return withDeleteEntityContext(entity.id) {
            fileResourceRepository.deleteUploadRecord(
                entity.id,
                requireNotNull(entity.uploadToken),
                entity.status,
            ).awaitFirstOrNull() == 1L
        }
    }

    override suspend fun markUploadCleanupPending(entity: FileResourceEntity): Boolean {
        return withInvalidateEntityCacheContext(entity.id) {
            fileResourceRepository.markCleanupPending(
                entity.id,
                requireNotNull(entity.uploadToken),
                FileResourceStatus.UPLOADING.typeId,
                FileResourceStatus.CLEANUP_PENDING.typeId,
            ).awaitFirstOrNull() == 1L
        }
    }

    override suspend fun cleanupUploads(now: Long): Long {
        var cleaned = 0L
        fileResourceRepository.findCleanupCandidates(
            now,
            FileResourceStatus.UPLOADING.typeId,
            FileResourceStatus.CLEANUP_PENDING.typeId,
        ).collectList().awaitFirstOrNull()?.forEach { entity ->
            if (entity.getRealStatus() == FileResourceStatus.UPLOADING) {
                if (!markUploadCleanupPending(entity)) {
                    return@forEach
                }
                entity.status = FileResourceStatus.CLEANUP_PENDING.typeId
            }
            val provider = storageProviderService.getByIdOrThrow(entity.storageProviderId)
            val error = fileResourceServiceManager.getService(provider).deleteObject(entity.objectKey)
            if (error == null && removeUploadRecord(entity)) {
                cleaned++
            }
        }
        return cleaned
    }

    override suspend fun getFileDownloadUrl(entity: FileResourceEntity, viewer: UserAuthentication?): String {
        // Mint-time access check: never hand out a URL the viewer is not entitled to read.
        resourceAccessService.assertReadable(entity, viewer)

        val visibility = resourceAccessService.resolveVisibility(entity.getRealResourceFileType())
        val signedUrlTtlSeconds = resourceAccessService.resolveSignedUrlTtlSeconds()
        val provider = storageProviderService.getByIdOrThrow(entity.storageProviderId)

        // Each provider decides how to honor visibility (stable URL for public resources, a short-lived
        // signature / vendor pre-signed URL for non-public ones). See AbstractFileResourceService.buildDownloadUrl.
        return fileResourceServiceManager.getService(provider).buildDownloadUrl(entity, visibility, signedUrlTtlSeconds)
    }

    override val cacheStore: ReactiveExpiringKVStore<String, FileResourceEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<FileResourceEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
    override val entityClass: KClass<FileResourceEntity> = FileResourceEntity::class
}