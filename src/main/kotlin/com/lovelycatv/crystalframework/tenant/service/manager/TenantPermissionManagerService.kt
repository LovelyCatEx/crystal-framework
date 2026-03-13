package com.lovelycatv.crystalframework.tenant.service.manager

import com.lovelycatv.crystalframework.cache.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData
import com.lovelycatv.crystalframework.tenant.controller.manager.permission.dto.ManagerCreateTenantPermissionDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.permission.dto.ManagerDeleteTenantPermissionDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.permission.dto.ManagerReadTenantPermissionDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.permission.dto.ManagerUpdateTenantPermissionDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantPermissionEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantPermissionRepository
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.shared.utils.toPaginatedResponseData
import com.lovelycatv.crystalframework.tenant.constants.TenantPermissionDeclaration
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.data.annotation.Id

interface TenantPermissionManagerService : CachedBaseManagerService<
        TenantPermissionRepository,
        TenantPermissionEntity,
        ManagerCreateTenantPermissionDTO,
        ManagerReadTenantPermissionDTO,
        ManagerUpdateTenantPermissionDTO,
        ManagerDeleteTenantPermissionDTO
> {
    override suspend fun query(
        dto: ManagerReadTenantPermissionDTO,
        isAdvanceQuery: suspend (dto: ManagerReadTenantPermissionDTO) -> Boolean,
        doAdvanceQuery: suspend (dto: ManagerReadTenantPermissionDTO, limit: Int, offset: Int) -> PaginatedResponseData<TenantPermissionEntity>
    ): PaginatedResponseData<TenantPermissionEntity> {
        return super.query(
            dto = dto,
            isAdvanceQuery = { dto.searchKeyword != null || dto.type != null },
            doAdvanceQuery = { readDto, limit, offset ->
                val total = getRepository().countAdvanceSearch(readDto.searchKeyword, readDto.type).awaitFirstOrNull() ?: 0
                val records = getRepository().advanceSearch(
                    readDto.searchKeyword,
                    readDto.type,
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
