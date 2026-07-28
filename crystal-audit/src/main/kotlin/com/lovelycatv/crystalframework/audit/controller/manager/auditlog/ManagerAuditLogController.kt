package com.lovelycatv.crystalframework.audit.controller.manager.auditlog

import com.lovelycatv.crystalframework.audit.controller.manager.auditlog.dto.ManagerCreateAuditLogDTO
import com.lovelycatv.crystalframework.audit.controller.manager.auditlog.dto.ManagerDeleteAuditLogDTO
import com.lovelycatv.crystalframework.audit.controller.manager.auditlog.dto.ManagerReadAuditLogDTO
import com.lovelycatv.crystalframework.audit.controller.manager.auditlog.dto.ManagerUpdateAuditLogDTO
import com.lovelycatv.crystalframework.audit.entity.AuditLogEntity
import com.lovelycatv.crystalframework.audit.repository.AuditLogRepository
import com.lovelycatv.crystalframework.audit.service.AuditLogManagerService
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.controller.PermissionMatrix
import com.lovelycatv.crystalframework.shared.controller.ReadonlyManagerController
import com.lovelycatv.crystalframework.shared.controller.systemOnlyReadonly
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
>(
    managerService,
    permissions = PermissionMatrix.systemOnlyReadonly(
        systemRead = PermissionMatrix.NOT_APPLICABLE,
        superRead = SystemPermission.ACTION_AUDIT_LOG_READ,
    ),
)
