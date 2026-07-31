package com.lovelycatv.crystalframework.audit.service

import com.lovelycatv.crystalframework.audit.context.AuditRequestInfo
import com.lovelycatv.crystalframework.audit.types.AuditAction
import com.lovelycatv.crystalframework.shared.types.UserAuthentication
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component

@Component
class AuditLogRecorder(
    private val auditLogService: AuditLogService,
) {
    private val logger = logger()
    private val auditScope = CoroutineScope(Dispatchers.IO)

    fun record(
        userAuthentication: UserAuthentication,
        auditRequestInfo: AuditRequestInfo?,
        action: AuditAction,
        resourceType: String,
        resourceIds: List<Long>?,
        success: Boolean,
        errorMessage: String?,
    ) {
        auditScope.launch {
            try {
                auditLogService.record(
                    userAuthentication = userAuthentication,
                    auditRequestInfo = auditRequestInfo,
                    action = action,
                    resourceType = resourceType,
                    resourceIds = resourceIds,
                    success = success,
                    errorMessage = errorMessage,
                )
            } catch (e: Exception) {
                logger.error("Failed to record audit log: ${e.message}", e)
            }
        }
    }
}
