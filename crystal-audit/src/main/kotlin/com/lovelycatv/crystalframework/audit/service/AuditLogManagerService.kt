/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.audit.service

import com.lovelycatv.crystalframework.audit.controller.manager.auditlog.dto.ManagerCreateAuditLogDTO
import com.lovelycatv.crystalframework.audit.controller.manager.auditlog.dto.ManagerDeleteAuditLogDTO
import com.lovelycatv.crystalframework.audit.controller.manager.auditlog.dto.ManagerReadAuditLogDTO
import com.lovelycatv.crystalframework.audit.controller.manager.auditlog.dto.ManagerUpdateAuditLogDTO
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
