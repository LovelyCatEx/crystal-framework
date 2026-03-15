package com.lovelycatv.crystalframework.tenant.service.manager

import com.lovelycatv.crystalframework.cache.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData
import com.lovelycatv.crystalframework.tenant.controller.manager.role.dto.ManagerCreateTenantRoleDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.role.dto.ManagerDeleteTenantRoleDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.role.dto.ManagerReadTenantRoleDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.role.dto.ManagerUpdateTenantRoleDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantRoleEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantRoleRepository
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.shared.utils.toPaginatedResponseData
import com.lovelycatv.crystalframework.tenant.constants.TenantRoleDeclaration
import com.lovelycatv.crystalframework.tenant.service.TenantRelationshipCheckService
import kotlinx.coroutines.reactive.awaitFirstOrNull

interface TenantRoleManagerService : CachedBaseManagerService<
        TenantRoleRepository,
        TenantRoleEntity,
        ManagerCreateTenantRoleDTO,
        ManagerReadTenantRoleDTO,
        ManagerUpdateTenantRoleDTO,
        ManagerDeleteTenantRoleDTO
>, TenantRelationshipCheckService {
    suspend fun createFromDeclaration(tenantId: Long, declaration: TenantRoleDeclaration): TenantRoleEntity

    override suspend fun query(
        dto: ManagerReadTenantRoleDTO,
        isAdvanceQuery: suspend (dto: ManagerReadTenantRoleDTO) -> Boolean,
        doAdvanceQuery: suspend (dto: ManagerReadTenantRoleDTO, limit: Int, offset: Int) -> PaginatedResponseData<TenantRoleEntity>
    ): PaginatedResponseData<TenantRoleEntity> {
        return super.query(
            dto = dto,
            isAdvanceQuery = { it.tenantId != null },
            doAdvanceQuery = { readDto, limit, offset ->
                val total = getRepository().countAdvanceSearch(readDto.searchKeyword, readDto.tenantId!!, readDto.parentId).awaitFirstOrNull() ?: 0
                val records = getRepository().advanceSearch(
                    readDto.searchKeyword,
                    readDto.tenantId,
                    readDto.parentId,
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
