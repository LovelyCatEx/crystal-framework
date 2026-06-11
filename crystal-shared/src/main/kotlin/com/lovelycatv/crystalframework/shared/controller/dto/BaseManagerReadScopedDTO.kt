package com.lovelycatv.crystalframework.shared.controller.dto

/**
 * Base read/query DTO for scoped resources.
 *
 * @property scope   The scope type identifier (e.g. 0=SYSTEM, 1=TENANT)
 * @property scopeId The ID within that scope (e.g. tenantId when scope=TENANT).
 *                   May be null for unrestricted queries (e.g. system admin full scan).
 */
open class BaseManagerReadScopedDTO(
    override val page: Int,
    override val pageSize: Int,
    open val scope: Int,
    open val scopeId: Long?,
) : BaseManagerReadDTO(page, pageSize)
