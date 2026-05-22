package com.lovelycatv.crystalframework.audit.controller.manager.auditlog

import com.lovelycatv.crystalframework.audit.entity.AuditLogEntity
import com.lovelycatv.crystalframework.audit.repository.AuditLogRepository
import com.lovelycatv.crystalframework.audit.service.AuditLogManagerService
import com.lovelycatv.crystalframework.shared.annotations.ManagerPermissions
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.ReadonlyManagerController
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ManagerPermissions(
    read = [SystemPermission.ACTION_AUDIT_LOG_READ],
    readAll = [SystemPermission.ACTION_AUDIT_LOG_READ],
    create = [SystemPermission.ACTION_AUDIT_LOG_CREATE],
    update = [SystemPermission.ACTION_AUDIT_LOG_UPDATE],
    delete = [SystemPermission.ACTION_AUDIT_LOG_DELETE],
)
@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/audit-log")
class ManagerAuditLogController(
    managerService: AuditLogManagerService
) : ReadonlyManagerController<
        AuditLogManagerService,
        AuditLogRepository,
        AuditLogEntity,
        ManagerCreateAuditLogDTO,
        ManagerReadAuditLogDTO,
        ManagerUpdateAuditLogDTO,
        ManagerDeleteAuditLogDTO
>(managerService)
