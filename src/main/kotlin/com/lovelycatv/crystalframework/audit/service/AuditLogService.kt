package com.lovelycatv.crystalframework.audit.service

import com.lovelycatv.crystalframework.audit.context.AuditRequestInfo
import com.lovelycatv.crystalframework.audit.types.AuditAction
import com.lovelycatv.crystalframework.shared.types.UserAuthentication

interface AuditLogService {
    suspend fun record(
        userAuthentication: UserAuthentication,
        auditRequestInfo: AuditRequestInfo?,
        action: AuditAction,
        resourceType: String,
        resourceIds: List<Long>?,
        success: Boolean,
        errorMessage: String?
    )
}
