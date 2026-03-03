package com.lovelycatv.crystalframework.resource.service.impl

import com.lovelycatv.crystalframework.resource.controller.manager.storage.dto.ManagerCreateStorageProviderDTO
import com.lovelycatv.crystalframework.resource.controller.manager.storage.dto.ManagerUpdateStorageProviderDTO
import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.repository.StorageProviderRepository
import com.lovelycatv.crystalframework.resource.service.StorageProviderManagerService
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service

@Service
class StorageProviderManagerServiceImpl(
    private val storageProviderRepository: StorageProviderRepository,
    private val snowIdGenerator: SnowIdGenerator
) : StorageProviderManagerService {
    override fun getRepository(): StorageProviderRepository {
        return this.storageProviderRepository
    }

    override suspend fun create(dto: ManagerCreateStorageProviderDTO): StorageProviderEntity {
        return this.getRepository().save(
            StorageProviderEntity(
                id = snowIdGenerator.nextId(),
                name = dto.name,
                description = dto.description,
                type = dto.type,
                baseUrl = dto.baseUrl,
                properties = dto.properties
            ) newEntity true
        ).awaitFirstOrNull() ?: throw BusinessException("Could not create storage provider")
    }

    override suspend fun applyDTOToEntity(
        dto: ManagerUpdateStorageProviderDTO,
        original: StorageProviderEntity
    ): StorageProviderEntity {
        return original.apply {
            if (dto.name != null) {
                this.name = dto.name
            }
            if (dto.description != null) {
                this.description = dto.description
            }
            if (dto.type != null) {
                this.type = dto.type
            }
            if (dto.baseUrl != null) {
                this.baseUrl = dto.baseUrl
            }
            if (dto.properties != null) {
                this.properties = dto.properties
            }
            if (dto.active != null) {
                this.active = dto.active
            }
        }
    }
}
