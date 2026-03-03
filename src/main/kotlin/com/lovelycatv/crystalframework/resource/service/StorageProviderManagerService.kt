package com.lovelycatv.crystalframework.resource.service

import com.lovelycatv.crystalframework.resource.controller.manager.storage.dto.ManagerCreateStorageProviderDTO
import com.lovelycatv.crystalframework.resource.controller.manager.storage.dto.ManagerDeleteStorageProviderDTO
import com.lovelycatv.crystalframework.resource.controller.manager.storage.dto.ManagerReadStorageProviderDTO
import com.lovelycatv.crystalframework.resource.controller.manager.storage.dto.ManagerUpdateStorageProviderDTO
import com.lovelycatv.crystalframework.resource.entity.StorageProviderEntity
import com.lovelycatv.crystalframework.resource.repository.StorageProviderRepository
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData
import com.lovelycatv.crystalframework.shared.service.BaseManagerService
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.shared.utils.toPaginatedResponseData
import kotlinx.coroutines.reactive.awaitFirstOrNull

interface StorageProviderManagerService : BaseManagerService<
        StorageProviderRepository,
        StorageProviderEntity,
        ManagerCreateStorageProviderDTO,
        ManagerReadStorageProviderDTO,
        ManagerUpdateStorageProviderDTO,
        ManagerDeleteStorageProviderDTO
> {
    override suspend fun query(
        dto: ManagerReadStorageProviderDTO,
        isAdvanceQuery: suspend (dto: ManagerReadStorageProviderDTO) -> Boolean,
        doAdvanceQuery: suspend (dto: ManagerReadStorageProviderDTO, limit: Int, offset: Int) -> PaginatedResponseData<StorageProviderEntity>
    ): PaginatedResponseData<StorageProviderEntity> {
        return super.query(
            dto,
            isAdvanceQuery = { dto ->
                dto.searchKeyword != null || dto.type != null
            },
            doAdvanceQuery = { dto, limit, offset ->
                val total = this.getRepository()
                    .countAdvanceSearch(
                        dto.searchKeyword,
                        dto.type,
                    )
                    .awaitFirstOrNull()
                    ?: 0

                val records = this.getRepository().advanceSearch(
                    dto.searchKeyword,
                    dto.type,
                    limit,
                    offset
                ).awaitListWithTimeout()

                dto.toPaginatedResponseData(
                    total = total,
                    records = records
                )
            }
        )
    }
}
