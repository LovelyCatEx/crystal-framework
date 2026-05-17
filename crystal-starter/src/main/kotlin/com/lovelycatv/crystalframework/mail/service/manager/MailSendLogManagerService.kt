package com.lovelycatv.crystalframework.mail.service.manager

import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.mail.controller.manager.dto.ManagerCreateMailSendLogDTO
import com.lovelycatv.crystalframework.mail.controller.manager.dto.ManagerDeleteMailSendLogDTO
import com.lovelycatv.crystalframework.mail.controller.manager.dto.ManagerReadMailSendLogDTO
import com.lovelycatv.crystalframework.mail.controller.manager.dto.ManagerUpdateMailSendLogDTO
import com.lovelycatv.crystalframework.mail.entity.MailSendLogEntity
import com.lovelycatv.crystalframework.mail.repository.MailSendLogRepository
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.shared.utils.toPaginatedResponseData
import kotlinx.coroutines.reactive.awaitFirstOrNull

interface MailSendLogManagerService : CachedBaseManagerService<
        MailSendLogRepository,
        MailSendLogEntity,
        ManagerCreateMailSendLogDTO,
        ManagerReadMailSendLogDTO,
        ManagerUpdateMailSendLogDTO,
        ManagerDeleteMailSendLogDTO
> {
    override suspend fun query(
        dto: ManagerReadMailSendLogDTO,
        isAdvanceQuery: suspend (dto: ManagerReadMailSendLogDTO) -> Boolean,
        doAdvanceQuery: suspend (dto: ManagerReadMailSendLogDTO, limit: Int, offset: Int) -> PaginatedResponseData<MailSendLogEntity>
    ): PaginatedResponseData<MailSendLogEntity> {
        return super.query(
            dto,
            isAdvanceQuery = { dto ->
                dto.searchKeyword != null || dto.toEmail != null || dto.success != null || dto.userId != null || dto.tenantId != null || dto.startTime != null || dto.endTime != null
            },
            doAdvanceQuery = { dto, limit, offset ->
                val total = this.getRepository()
                    .countAdvanceSearch(
                        dto.searchKeyword,
                        dto.toEmail,
                        dto.success,
                        dto.userId,
                        dto.tenantId,
                        dto.startTime,
                        dto.endTime
                    )
                    .awaitFirstOrNull()
                    ?: 0

                val records = this.getRepository().advanceSearch(
                    dto.searchKeyword,
                    dto.toEmail,
                    dto.success,
                    dto.userId,
                    dto.tenantId,
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

    override suspend fun create(dto: ManagerCreateMailSendLogDTO): MailSendLogEntity {
        throw BusinessException("Mail send logs cannot be created manually")
    }

    override suspend fun applyDTOToEntity(dto: ManagerUpdateMailSendLogDTO, original: MailSendLogEntity): MailSendLogEntity {
        throw BusinessException("Mail send logs cannot be updated")
    }
}