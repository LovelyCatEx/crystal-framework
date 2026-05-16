package com.lovelycatv.crystalframework.mail.service.impl

import com.lovelycatv.crystalframework.mail.entity.MailSendLogEntity
import com.lovelycatv.crystalframework.mail.repository.MailSendLogRepository
import com.lovelycatv.crystalframework.mail.service.MailSendLogService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service

@Service
class MailSendLogServiceImpl(
    private val mailSendLogRepository: MailSendLogRepository,
    private val snowIdGenerator: SnowIdGenerator
) : MailSendLogService {

    override suspend fun record(
        fromEmail: String,
        toEmail: String,
        subject: String,
        content: String,
        success: Boolean,
        errorMessage: String?,
        userId: Long?,
        tenantId: Long?
    ) {
        val entity = MailSendLogEntity(
            id = snowIdGenerator.nextId(),
            fromEmail = fromEmail,
            toEmail = toEmail,
            subject = subject,
            content = content,
            success = success,
            errorMessage = errorMessage,
            userId = userId,
            tenantId = tenantId
        ).apply { newEntity() }

        mailSendLogRepository.save(entity).awaitFirstOrNull()
    }
}