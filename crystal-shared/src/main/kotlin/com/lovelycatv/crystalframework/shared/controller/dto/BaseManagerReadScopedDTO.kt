package com.lovelycatv.crystalframework.shared.controller.dto

/**
 * Base read/query DTO for scoped resources.
 *
 * @property scope   The scope type identifier (e.g. 0=SYSTEM, 1=TENANT).
 *                   Defaults to 0 so that id-only lookups (where the service short-circuits
 *                   via [com.lovelycatv.crystalframework.shared.service.BaseManagerService.query]
 *                   on `dto.id != null`) succeed without callers having to send scope.
 * @property scopeId The ID within that scope (e.g. tenantId when scope=TENANT).
 *                   Defaults to 0 for the same reason.
 */
open class BaseManagerReadScopedDTO(
    override val page: Int,
    override val pageSize: Int,
    open val scope: Int = 0,
    open val scopeId: Long = 0,
) : BaseManagerReadDTO(page, pageSize)
