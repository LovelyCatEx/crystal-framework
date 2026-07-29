package com.lovelycatv.crystalframework.audit.controller.manager.auditlog.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerUpdateDTO

/**
 * Audit logs are immutable and should not be updated.
 * This DTO exists only to satisfy the generic type constraint of StandardManagerController.
 */
data class ManagerUpdateAuditLogDTO(
    override val id: Long
) : BaseManagerUpdateDTO(id)
