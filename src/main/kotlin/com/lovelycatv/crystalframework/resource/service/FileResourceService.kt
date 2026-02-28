package com.lovelycatv.crystalframework.resource.service

import com.lovelycatv.crystalframework.resource.entity.FileResourceEntity
import com.lovelycatv.crystalframework.resource.repository.FileResourceRepository
import com.lovelycatv.crystalframework.shared.service.BaseService

interface FileResourceService : BaseService<FileResourceRepository, FileResourceEntity> {
    fun generateNextSnowId(gene: Long = 0): Long

    suspend fun getByMD5(md5: String): FileResourceEntity?

    suspend fun getFileDownloadUrl(entity: FileResourceEntity): String
}