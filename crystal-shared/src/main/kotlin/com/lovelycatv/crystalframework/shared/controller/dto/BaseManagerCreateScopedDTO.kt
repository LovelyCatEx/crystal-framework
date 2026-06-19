package com.lovelycatv.crystalframework.shared.controller.dto

/**
 * Base create DTO for scoped resources.
 *
 * @property scope  The scope type identifier (e.g. 0=SYSTEM, 1=TENANT)
 * @property scopeId The ID within that scope (e.g. tenantId when scope=TENANT)
 */
open class BaseManagerCreateScopedDTO(
    open val scope: Int,
    open val scopeId: Long,
)
