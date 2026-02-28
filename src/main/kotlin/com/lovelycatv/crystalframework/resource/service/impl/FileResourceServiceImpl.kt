package com.lovelycatv.crystalframework.resource.service.impl

import com.lovelycatv.crystalframework.resource.entity.FileResourceEntity
import com.lovelycatv.crystalframework.resource.repository.FileResourceRepository
import com.lovelycatv.crystalframework.resource.service.FileResourceService
import com.lovelycatv.crystalframework.resource.service.StorageProviderService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service

@Service
class FileResourceServiceImpl(
    private val fileResourceRepository: FileResourceRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val storageProviderService: StorageProviderService
) : FileResourceService {
    override fun getRepository(): FileResourceRepository {
        return this.fileResourceRepository
    }

    override fun generateNextSnowId(gene: Long): Long {
        return snowIdGenerator.nextId(gene)
    }

    override suspend fun getByMD5(md5: String): FileResourceEntity? {
        return this.getRepository()
            .findByMd5(md5)
            .awaitFirstOrNull()
    }

    override suspend fun getFileDownloadUrl(entity: FileResourceEntity): String {
        return storageProviderService
            .getByIdOrThrow(entity.storageProviderId).baseUrl + entity.objectKey
    }
}