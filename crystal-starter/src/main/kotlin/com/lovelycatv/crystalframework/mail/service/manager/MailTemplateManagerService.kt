package com.lovelycatv.crystalframework.mail.service.manager

import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService
import com.lovelycatv.crystalframework.mail.controller.manager.template.dto.ManagerCreateMailTemplateDTO
import com.lovelycatv.crystalframework.mail.controller.manager.template.dto.ManagerDeleteMailTemplateDTO
import com.lovelycatv.crystalframework.mail.controller.manager.template.dto.ManagerReadMailTemplateDTO
import com.lovelycatv.crystalframework.mail.controller.manager.template.dto.ManagerUpdateMailTemplateDTO
import com.lovelycatv.crystalframework.mail.entity.MailTemplateEntity
import com.lovelycatv.crystalframework.mail.repository.MailTemplateRepository
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.crystalframework.shared.utils.toPaginatedResponseData
import kotlinx.coroutines.reactive.awaitFirstOrNull

interface MailTemplateManagerService : CachedBaseManagerService<
        MailTemplateRepository,
        MailTemplateEntity,
        ManagerCreateMailTemplateDTO,
        ManagerReadMailTemplateDTO,
        ManagerUpdateMailTemplateDTO,
        ManagerDeleteMailTemplateDTO
> {
    override suspend fun query(
        dto: ManagerReadMailTemplateDTO,
        isAdvanceQuery: suspend (dto: ManagerReadMailTemplateDTO) -> Boolean,
        doAdvanceQuery: suspend (dto: ManagerReadMailTemplateDTO, limit: Int, offset: Int) -> PaginatedResponseData<MailTemplateEntity>
    ): PaginatedResponseData<MailTemplateEntity> {
        return super.query(
            dto,
            isAdvanceQuery = { dto ->
                dto.searchKeyword != null || dto.typeId != null || dto.startTime != null || dto.endTime != null
            },
            doAdvanceQuery = { dto, limit, offset ->
                val total = this.getRepository()
                    .countAdvanceSearch(
                        dto.searchKeyword,
                        dto.typeId,
                        dto.startTime,
                        dto.endTime,
                    )
                    .awaitFirstOrNull()
                    ?: 0

                val records = this.getRepository().advanceSearch(
                    dto.searchKeyword,
                    dto.typeId,
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
