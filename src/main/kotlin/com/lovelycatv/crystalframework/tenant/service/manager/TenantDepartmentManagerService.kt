package com.lovelycatv.crystalframework.tenant.service.manager

import com.lovelycatv.crystalframework.cache.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData
import com.lovelycatv.crystalframework.tenant.controller.manager.department.dto.ManagerCreateTenantDepartmentDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.dto.ManagerDeleteTenantDepartmentDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.dto.ManagerReadTenantDepartmentDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.dto.ManagerUpdateTenantDepartmentDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantDepartmentEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantDepartmentRepository
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.shared.utils.toPaginatedResponseData
import com.lovelycatv.crystalframework.tenant.service.TenantRelationshipCheckService
import kotlinx.coroutines.reactive.awaitFirstOrNull

interface TenantDepartmentManagerService : CachedBaseManagerService<
        TenantDepartmentRepository,
        TenantDepartmentEntity,
        ManagerCreateTenantDepartmentDTO,
        ManagerReadTenantDepartmentDTO,
        ManagerUpdateTenantDepartmentDTO,
        ManagerDeleteTenantDepartmentDTO
>, TenantRelationshipCheckService {
    override suspend fun query(
        dto: ManagerReadTenantDepartmentDTO,
        isAdvanceQuery: suspend (dto: ManagerReadTenantDepartmentDTO) -> Boolean,
        doAdvanceQuery: suspend (dto: ManagerReadTenantDepartmentDTO, limit: Int, offset: Int) -> PaginatedResponseData<TenantDepartmentEntity>
    ): PaginatedResponseData<TenantDepartmentEntity> {
        return super.query(
            dto = dto,
            isAdvanceQuery = { dto.tenantId != null || dto.searchKeyword != null || dto.parentId != null },
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
