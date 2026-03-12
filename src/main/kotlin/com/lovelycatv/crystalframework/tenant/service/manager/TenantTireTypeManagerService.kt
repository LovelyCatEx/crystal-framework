package com.lovelycatv.crystalframework.tenant.service.manager

import com.lovelycatv.crystalframework.cache.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData
import com.lovelycatv.crystalframework.tenant.entity.TenantTireTypeEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantTireTypeRepository
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.shared.utils.toPaginatedResponseData
import com.lovelycatv.crystalframework.tenant.controller.manager.tire.dto.ManagerCreateTenantTireTypeDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.tire.dto.ManagerDeleteTenantTireTypeDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.tire.dto.ManagerReadTenantTireTypeDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.tire.dto.ManagerUpdateTenantTireTypeDTO
import kotlinx.coroutines.reactive.awaitFirstOrNull

interface TenantTireTypeManagerService : CachedBaseManagerService<
        TenantTireTypeRepository,
        TenantTireTypeEntity,
        ManagerCreateTenantTireTypeDTO,
        ManagerReadTenantTireTypeDTO,
        ManagerUpdateTenantTireTypeDTO,
        ManagerDeleteTenantTireTypeDTO
        > {
    override suspend fun query(
        dto: ManagerReadTenantTireTypeDTO,
        isAdvanceQuery: suspend (dto: ManagerReadTenantTireTypeDTO) -> Boolean,
        doAdvanceQuery: suspend (dto: ManagerReadTenantTireTypeDTO, limit: Int, offset: Int) -> PaginatedResponseData<TenantTireTypeEntity>
    ): PaginatedResponseData<TenantTireTypeEntity> {
        return super.query(
            dto = dto,
            isAdvanceQuery = { dto.searchKeyword != null },
            doAdvanceQuery = { readDto, limit, offset ->
                val total = getRepository().countByKeyword(readDto.searchKeyword!!).awaitFirstOrNull() ?: 0
                val records = getRepository().searchByKeyword(
                    readDto.searchKeyword!!,
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
