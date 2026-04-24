package com.lovelycatv.crystalframework.tenant.service.manager

import com.lovelycatv.crystalframework.cache.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData
import com.lovelycatv.crystalframework.tenant.controller.manager.invitation.dto.ManagerCreateInvitationDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.invitation.dto.ManagerDeleteInvitationDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.invitation.dto.ManagerReadInvitationDTO
import com.lovelycatv.crystalframework.tenant.controller.manager.invitation.dto.ManagerUpdateInvitationDTO
import com.lovelycatv.crystalframework.tenant.entity.TenantInvitationEntity
import com.lovelycatv.crystalframework.tenant.repository.TenantInvitationRepository
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.shared.utils.toPaginatedResponseData
import com.lovelycatv.crystalframework.tenant.service.TenantRelationshipCheckService
import kotlinx.coroutines.reactive.awaitFirstOrNull

interface TenantInvitationManagerService : BaseTenantResourceManagerService<
        TenantInvitationRepository,
        TenantInvitationEntity,
        ManagerCreateInvitationDTO,
        ManagerReadInvitationDTO,
        ManagerUpdateInvitationDTO,
        ManagerDeleteInvitationDTO
> {
    override suspend fun query(
        dto: ManagerReadInvitationDTO,
        isAdvanceQuery: suspend (dto: ManagerReadInvitationDTO) -> Boolean,
        doAdvanceQuery: suspend (dto: ManagerReadInvitationDTO, limit: Int, offset: Int) -> PaginatedResponseData<TenantInvitationEntity>
    ): PaginatedResponseData<TenantInvitationEntity> {
        return super.query(
            dto = dto,
            isAdvanceQuery = { it.tenantId != null },
            doAdvanceQuery = { readDto, limit, offset ->
                val total = getRepository().countAdvanceSearch(
                    readDto.searchKeyword,
                    readDto.tenantId!!
                ).awaitFirstOrNull() ?: 0

                val records = getRepository().advanceSearch(
                    readDto.searchKeyword,
                    readDto.tenantId,
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