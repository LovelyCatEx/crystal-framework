package com.lovelycatv.crystalframework.audit.controller.manager.auditlog.dto

/**
 * Audit logs are created automatically by the system, not manually.
 * This DTO exists only to satisfy the generic type constraint of StandardManagerController.
 */
data class ManagerCreateAuditLogDTO(
    val placeholder: String? = null
)
