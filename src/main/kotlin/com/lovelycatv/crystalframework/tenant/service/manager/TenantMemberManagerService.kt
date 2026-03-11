package com.lovelycatv.crystalframework.tenant.service.manager

import com.lovelycatv.crystalframework.cache.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerCreateTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerDeleteTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerReadTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.member.dto.ManagerUpdateTenantMemberDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.member.vo.TenantMemberVO
import com.lovelycatv.crystalframework.tenant.entity.TenantMemberEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantMemberRepository
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.shared.utils.toPaginatedResponseData
import kotlinx.coroutines.reactive.awaitFirstOrNull

interface TenantMemberManagerService : CachedBaseManagerService<
        TenantMemberRepository,
        TenantMemberEntity,
        ManagerCreateTenantMemberDTO,
        ManagerReadTenantMemberDTO,
        ManagerUpdateTenantMemberDTO,
        ManagerDeleteTenantMemberDTO
> {
    override suspend fun query(
        dto: ManagerReadTenantMemberDTO,
        isAdvanceQuery: suspend (dto: ManagerReadTenantMemberDTO) -> Boolean,
        doAdvanceQuery: suspend (dto: ManagerReadTenantMemberDTO, limit: Int, offset: Int) -> PaginatedResponseData<TenantMemberEntity>
    ): PaginatedResponseData<TenantMemberEntity> {
        return super.query(
            dto = dto,
            isAdvanceQuery = { dto.searchKeyword != null || dto.status != null },
            doAdvanceQuery = { readDto, limit, offset ->
                val total = getRepository().countAdvanceSearch(
                    readDto.searchKeyword,
                    readDto.tenantId,
                    readDto.status
                ).awaitFirstOrNull() ?: 0

                val records = getRepository().advanceSearch(
                    readDto.searchKeyword,
                    readDto.tenantId,
                    readDto.status,
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

    suspend fun queryVO(dto: ManagerReadTenantMemberDTO): PaginatedResponseData<TenantMemberVO>
}
