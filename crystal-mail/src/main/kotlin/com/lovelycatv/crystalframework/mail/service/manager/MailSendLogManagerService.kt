package com.lovelycatv.crystalframework.mail.service.manager

import com.lovelycatv.crystalframework.mail.controller.manager.dto.ManagerCreateMailSendLogDTO
import com.lovelycatv.crystalframework.mail.controller.manager.dto.ManagerDeleteMailSendLogDTO
import com.lovelycatv.crystalframework.mail.controller.manager.dto.ManagerReadMailSendLogDTO
import com.lovelycatv.crystalframework.mail.controller.manager.dto.ManagerUpdateMailSendLogDTO
import com.lovelycatv.crystalframework.mail.entity.MailSendLogEntity
import com.lovelycatv.crystalframework.mail.repository.MailSendLogRepository
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService

interface MailSendLogManagerService : CachedBaseManagerService<
        MailSendLogRepository,
        MailSendLogEntity,
        ManagerCreateMailSendLogDTO,
        ManagerReadMailSendLogDTO,
        ManagerUpdateMailSendLogDTO,
        ManagerDeleteMailSendLogDTO
> {
    override suspend fun create(dto: ManagerCreateMailSendLogDTO): MailSendLogEntity {
        throw BusinessException("Mail send logs cannot be created manually")
    }

    override suspend fun applyDTOToEntity(dto: ManagerUpdateMailSendLogDTO, original: MailSendLogEntity): MailSendLogEntity {
        throw BusinessException("Mail send logs cannot be updated")
    }
}
