package com.lovelycatv.crystalframework.tenant.service.manager

import com.lovelycatv.crystalframework.cache.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto.ManagerCreateTenantDepartmentMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto.ManagerDeleteTenantDepartmentMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto.ManagerReadTenantDepartmentMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.dto.ManagerUpdateTenantDepartmentMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.department.member.vo.TenantDepartmentMemberVO
import com.lovelycatv.crystalframework.tenant.entity.TenantDepartmentMemberRelationEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantDepartmentMemberRelationRepository
import com.lovelycatv.crystalframework.tenant.service.TenantRelationshipCheckService
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.shared.utils.toPaginatedResponseData
import kotlinx.coroutines.reactive.awaitFirstOrNull

interface TenantDepartmentMemberManagerService : BaseTenantResourceManagerService<
        TenantDepartmentMemberRelationRepository,
        TenantDepartmentMemberRelationEntity,
        ManagerCreateTenantDepartmentMemberDTO,
        ManagerReadTenantDepartmentMemberDTO,
        ManagerUpdateTenantDepartmentMemberDTO,
        ManagerDeleteTenantDepartmentMemberDTO
> {
    override suspend fun query(
        dto: ManagerReadTenantDepartmentMemberDTO,
        isAdvanceQuery: suspend (dto: ManagerReadTenantDepartmentMemberDTO) -> Boolean,
        doAdvanceQuery: suspend (dto: ManagerReadTenantDepartmentMemberDTO, limit: Int, offset: Int) -> PaginatedResponseData<TenantDepartmentMemberRelationEntity>
    ): PaginatedResponseData<TenantDepartmentMemberRelationEntity> {
        return super.query(
            dto = dto,
            isAdvanceQuery = { true },
            doAdvanceQuery = { readDto, limit, offset ->
                val total = getRepository().countAdvanceSearch(
                    readDto.departmentId,
                    readDto.memberId,
                    readDto.roleType
                ).awaitFirstOrNull() ?: 0

                val records = getRepository().advanceSearch(
                    readDto.departmentId,
                    readDto.memberId,
                    readDto.roleType,
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

    suspend fun queryVO(dto: ManagerReadTenantDepartmentMemberDTO): PaginatedResponseData<TenantDepartmentMemberVO>
}
