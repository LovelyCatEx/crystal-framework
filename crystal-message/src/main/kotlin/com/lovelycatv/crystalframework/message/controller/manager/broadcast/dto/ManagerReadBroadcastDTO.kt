package com.lovelycatv.crystalframework.message.controller.manager.broadcast.dto

import com.lovelycatv.crystalframework.shared.controller.dto.BaseManagerReadScopedDTO
import com.lovelycatv.crystalframework.shared.database.QueryNode

/**
 * Query broadcasts within a scope. `scope + scopeId` feed the scoped-read authorization; the
 * default [com.lovelycatv.crystalframework.shared.controller.StandardScopedManagerController]
 * read hook reads them directly.
 */
data class ManagerReadBroadcastDTO(
    override val page: Int,
    override val pageSize: Int,
    override val id: Long? = null,
    override val query: QueryNode? = null,
    override val scope: Int = 0,
    override val scopeId: Long = 0,
) : BaseManagerReadScopedDTO(page, pageSize, scope, scopeId)
