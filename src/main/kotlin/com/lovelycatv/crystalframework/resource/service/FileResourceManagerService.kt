package com.lovelycatv.crystalframework.resource.service

import com.lovelycatv.crystalframework.cache.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.resource.controller.manager.file.dto.ManagerCreateFileResourceDTO
import com.lovelycatv.crystalframework.resource.controller.manager.file.dto.ManagerDeleteFileResourceDTO
import com.lovelycatv.crystalframework.resource.controller.manager.file.dto.ManagerReadFileResourceDTO
import com.lovelycatv.crystalframework.resource.controller.manager.file.dto.ManagerUpdateFileResourceDTO
import com.lovelycatv.crystalframework.resource.entity.FileResourceEntity
import com.lovelycatv.crystalframework.resource.repository.FileResourceRepository
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.shared.utils.toPaginatedResponseData
import kotlinx.coroutines.reactive.awaitFirstOrNull

interface FileResourceManagerService : CachedBaseManagerService<
        FileResourceRepository,
        FileResourceEntity,
        ManagerCreateFileResourceDTO,
        ManagerReadFileResourceDTO,
        ManagerUpdateFileResourceDTO,
        ManagerDeleteFileResourceDTO
> {
    override suspend fun query(
        dto: ManagerReadFileResourceDTO,
        isAdvanceQuery: suspend (dto: ManagerReadFileResourceDTO) -> Boolean,
        doAdvanceQuery: suspend (dto: ManagerReadFileResourceDTO, limit: Int, offset: Int) -> PaginatedResponseData<FileResourceEntity>
    ): PaginatedResponseData<FileResourceEntity> {
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
