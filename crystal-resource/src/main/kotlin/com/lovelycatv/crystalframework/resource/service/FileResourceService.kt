package com.lovelycatv.crystalframework.resource.service

import com.lovelycatv.crystalframework.shared.config.CrystalFrameworkConfiguration
import com.lovelycatv.crystalframework.resource.entity.FileResourceEntity
import com.lovelycatv.crystalframework.resource.repository.FileResourceRepository
import com.lovelycatv.crystalframework.resource.types.ResourceFileType
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.CachedBaseService

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

    suspend fun getByMD5(md5: String): FileResourceEntity?

    suspend fun getFileDownloadUrl(entity: FileResourceEntity): String

    suspend fun getFileDownloadUrl(entityId: Long?): String? {
        return this.getByIdOrNull(entityId)?.let {
            this.getFileDownloadUrl(it)
        }
    }
}