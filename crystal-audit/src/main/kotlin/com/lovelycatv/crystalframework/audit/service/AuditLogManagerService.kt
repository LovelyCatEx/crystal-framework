package com.lovelycatv.crystalframework.audit.service

import com.lovelycatv.crystalframework.audit.controller.manager.dto.ManagerCreateAuditLogDTO
import com.lovelycatv.crystalframework.audit.controller.manager.dto.ManagerDeleteAuditLogDTO
import com.lovelycatv.crystalframework.audit.controller.manager.dto.ManagerReadAuditLogDTO
import com.lovelycatv.crystalframework.audit.controller.manager.dto.ManagerUpdateAuditLogDTO
import com.lovelycatv.crystalframework.audit.entity.AuditLogEntity
import com.lovelycatv.crystalframework.audit.repository.AuditLogRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.shared.utils.toPaginatedResponseData
import kotlinx.coroutines.reactive.awaitFirstOrNull

interface AuditLogManagerService : CachedBaseManagerService<
        AuditLogRepository,
        AuditLogEntity,
        ManagerCreateAuditLogDTO,
        ManagerReadAuditLogDTO,
        ManagerUpdateAuditLogDTO,
        ManagerDeleteAuditLogDTO
> {
    override suspend fun query(
        dto: ManagerReadAuditLogDTO,
        isAdvanceQuery: suspend (dto: ManagerReadAuditLogDTO) -> Boolean,
        doAdvanceQuery: suspend (dto: ManagerReadAuditLogDTO, limit: Int, offset: Int) -> PaginatedResponseData<AuditLogEntity>
    ): PaginatedResponseData<AuditLogEntity> {
        return super.query(
            dto,
            isAdvanceQuery = { dto ->
                dto.searchKeyword != null || dto.userId != null || dto.username != null || dto.action != null || dto.path != null || dto.remoteIp != null || dto.startTime != null || dto.endTime != null
            },
            doAdvanceQuery = { dto, limit, offset ->
                val total = this.getRepository()
                    .countAdvanceSearch(
                        dto.searchKeyword,
                        dto.userId,
                        dto.username,
                        dto.action,
                        dto.path,
                        dto.remoteIp,
                        dto.startTime,
                        dto.endTime
                    )
                    .awaitFirstOrNull()
                    ?: 0

                val records = this.getRepository().advanceSearch(
                    dto.searchKeyword,
                    dto.userId,
                    dto.username,
                    dto.action,
                    dto.path,
                    dto.remoteIp,
                    dto.startTime,
                    dto.endTime,
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
