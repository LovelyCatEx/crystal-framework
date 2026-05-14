package com.lovelycatv.crystalframework.audit.service.impl

import com.lovelycatv.crystalframework.audit.context.AuditRequestInfo
import com.lovelycatv.crystalframework.audit.entity.AuditLogEntity
import com.lovelycatv.crystalframework.audit.repository.AuditLogRepository
import com.lovelycatv.crystalframework.audit.service.AuditLogService
import com.lovelycatv.crystalframework.audit.types.AuditAction
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service

@Service
class AuditLogServiceImpl(
    private val auditLogRepository: AuditLogRepository,
    private val snowIdGenerator: SnowIdGenerator
) : AuditLogService {

    override suspend fun record(
        userAuthentication: UserAuthentication,
        auditRequestInfo: AuditRequestInfo?,
        action: AuditAction,
        resourceType: String,
        resourceIds: List<Long>?,
        success: Boolean,
        errorMessage: String?
    ) {
        val entity = AuditLogEntity(
            id = snowIdGenerator.nextId(),
            userId = userAuthentication.userId,
            username = userAuthentication.username,
            tenantId = userAuthentication.tenantId,
            action = action.code,
            resourceType = resourceType,
            resourceIds = resourceIds?.joinToString(","),
            requestId = auditRequestInfo?.requestId,
            httpMethod = auditRequestInfo?.httpMethod,
            path = auditRequestInfo?.path,
            remoteIp = auditRequestInfo?.remoteIp,
            userAgent = auditRequestInfo?.userAgent,
            success = success,
            errorMessage = errorMessage
        ).apply { newEntity() }

        auditLogRepository.save(entity).awaitFirstOrNull()
    }
}
