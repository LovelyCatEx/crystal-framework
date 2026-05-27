package com.lovelycatv.crystalframework.audit.service

import com.lovelycatv.crystalframework.audit.controller.manager.auditlog.ManagerCreateAuditLogDTO
import com.lovelycatv.crystalframework.audit.controller.manager.auditlog.ManagerDeleteAuditLogDTO
import com.lovelycatv.crystalframework.audit.controller.manager.auditlog.ManagerReadAuditLogDTO
import com.lovelycatv.crystalframework.audit.controller.manager.auditlog.ManagerUpdateAuditLogDTO
import com.lovelycatv.crystalframework.audit.entity.AuditLogEntity
import com.lovelycatv.crystalframework.audit.repository.AuditLogRepository
import com.lovelycatv.crystalframework.shared.service.CachedBaseManagerService

interface AuditLogManagerService : CachedBaseManagerService<
        AuditLogRepository,
        AuditLogEntity,
        ManagerCreateAuditLogDTO,
        ManagerReadAuditLogDTO,
        ManagerUpdateAuditLogDTO,
        ManagerDeleteAuditLogDTO
>
