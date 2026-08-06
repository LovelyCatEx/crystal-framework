package com.lovelycatv.crystalframework.resource.service

import com.lovelycatv.crystalframework.shared.config.CrystalFrameworkConfiguration
import com.lovelycatv.crystalframework.resource.entity.FileResourceEntity
import com.lovelycatv.crystalframework.resource.repository.FileResourceRepository
import com.lovelycatv.crystalframework.resource.types.ResourceFileType
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.CachedBaseService
import com.lovelycatv.crystalframework.shared.types.UserAuthentication

interface FileResourceService : CachedBaseService<FileResourceRepository, FileResourceEntity> {
    fun generateNextSnowId(gene: Long = 0): Long

    fun getCrystalFrameworkConfiguration(): CrystalFrameworkConfiguration

    fun assertFileContentType(
        fileType: ResourceFileType,
        contentType: String,
        t: Throwable = BusinessException("unsupported content type $contentType")
    ) {
        if (!this.checkFileContentType(fileType, contentType)) {
            throw t
        }
    }

    fun checkFileContentType(fileType: ResourceFileType, contentType: String): Boolean

    fun resolveFileExtension(
        fileType: ResourceFileType,
        contentType: String,
        requestedExtension: String
    ): String?

    suspend fun getCommitedFileByMD5(md5: String, scope: Int, scopeId: Long): FileResourceEntity?

    suspend fun reservePreUpload(entity: FileResourceEntity): FileResourceEntity?

    suspend fun commitUpload(entity: FileResourceEntity): Boolean

    suspend fun removeUploadRecord(entity: FileResourceEntity): Boolean

    suspend fun markUploadCleanupPending(entity: FileResourceEntity): Boolean

    suspend fun cleanupUploads(now: Long): Long

    /**
     * Resolves a readable download URL for [entity]. Enforces [viewer]'s read entitlement via
     * [ResourceAccessService.assertReadable] before minting the URL (mint-time check). Local
     * non-public resources receive a short-lived HMAC-signed URL so a browser `<img>` tag can
     * load them without an Authorization header; OSS/COS return the provider URL unchanged.
     */
    suspend fun getFileDownloadUrl(entity: FileResourceEntity, viewer: UserAuthentication?): String

    suspend fun getFileDownloadUrl(entityId: Long?, viewer: UserAuthentication?): String? {
        return this.getByIdOrNull(entityId)?.let {
            this.getFileDownloadUrl(it, viewer)
        }
    }
}