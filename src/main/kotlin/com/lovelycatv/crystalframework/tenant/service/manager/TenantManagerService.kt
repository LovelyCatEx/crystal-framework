package com.lovelycatv.crystalframework.tenant.service.manager

import com.lovelycatv.crystalframework.cache.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.shared.utils.toPaginatedResponseData
import com.lovelycatv.crystalframework.tenant.controller.manager.tenant.dto.ManagerCreateTenantDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.tenant.dto.ManagerDeleteTenantDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.tenant.dto.ManagerReadTenantDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.tenant.dto.ManagerUpdateTenantDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantRepository
import kotlinx.coroutines.reactive.awaitFirstOrNull

interface TenantManagerService : CachedBaseManagerService<
        TenantRepository,
        TenantEntity,
        ManagerCreateTenantDTO,
        ManagerReadTenantDTO,
        ManagerUpdateTenantDTO,
        ManagerDeleteTenantDTO
> {
    override suspend fun query(
        dto: ManagerReadTenantDTO,
        isAdvanceQuery: suspend (dto: ManagerReadTenantDTO) -> Boolean,
        doAdvanceQuery: suspend (dto: ManagerReadTenantDTO, limit: Int, offset: Int) -> PaginatedResponseData<TenantEntity>
    ): PaginatedResponseData<TenantEntity> {
        return super.query(
            dto = dto,
            isAdvanceQuery = { dto.searchKeyword != null || dto.status != null || dto.startTime != null || dto.endTime != null },
            doAdvanceQuery = { readDto, limit, offset ->
                val total = getRepository().countAdvanceSearch(readDto.searchKeyword, readDto.status, readDto.startTime, readDto.endTime).awaitFirstOrNull() ?: 0
                val records = getRepository().advanceSearch(
                    readDto.searchKeyword,
                    readDto.status,
                    readDto.startTime,
                    readDto.endTime,
                    limit,
                    offset
                ).awaitListWithTimeout()

                readDto.toPaginatedResponseData(
                    total = total,
                    records = records
                )
            }
        )
    }
}
