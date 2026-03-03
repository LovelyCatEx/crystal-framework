package com.lovelycatv.crystalframework.resource.service.impl

import com.lovelycatv.crystalframework.resource.controller.manager.file.dto.ManagerCreateFileResourceDTO
import com.lovelycatv.crystalframework.resource.controller.manager.file.dto.ManagerUpdateFileResourceDTO
import com.lovelycatv.crystalframework.resource.entity.FileResourceEntity
import com.lovelycatv.crystalframework.resource.repository.FileResourceRepository
import com.lovelycatv.crystalframework.resource.service.FileResourceManagerService
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service

@Service
class FileResourceManagerServiceImpl(
    private val fileResourceRepository: FileResourceRepository,
    private val snowIdGenerator: SnowIdGenerator
) : FileResourceManagerService {
    override fun getRepository(): FileResourceRepository {
        return this.fileResourceRepository
    }

    override suspend fun create(dto: ManagerCreateFileResourceDTO): FileResourceEntity {
        return this.getRepository().save(
            FileResourceEntity(
                id = snowIdGenerator.nextId(),
                userId = dto.userId,
                type = dto.type,
                fileName = dto.fileName,
                fileExtension = dto.fileExtension,
                md5 = dto.md5,
                fileSize = dto.fileSize,
                storageProviderId = dto.storageProviderId,
                objectKey = dto.objectKey
            ) newEntity true
        ).awaitFirstOrNull() ?: throw BusinessException("Could not create file resource")
    }

    override suspend fun applyDTOToEntity(
        dto: ManagerUpdateFileResourceDTO,
        original: FileResourceEntity
    ): FileResourceEntity {
        return original.apply {
            if (dto.userId != null) {
                this.userId = dto.userId
            }
            if (dto.type != null) {
                this.type = dto.type
            }
            if (dto.fileName != null) {
                this.fileName = dto.fileName
            }
            if (dto.fileExtension != null) {
                this.fileExtension = dto.fileExtension
            }
            if (dto.md5 != null) {
                this.md5 = dto.md5
            }
            if (dto.fileSize != null) {
                this.fileSize = dto.fileSize
            }
            if (dto.storageProviderId != null) {
                this.storageProviderId = dto.storageProviderId
            }
            if (dto.objectKey != null) {
                this.objectKey = dto.objectKey
            }
        }
    }
}
