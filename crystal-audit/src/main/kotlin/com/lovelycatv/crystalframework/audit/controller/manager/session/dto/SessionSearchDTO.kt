package com.lovelycatv.crystalframework.audit.controller.manager.session.dto

import com.lovelycatv.crystalframework.shared.request.PageQuery

data class SessionSearchDTO(
    override val page: Int,
    override val pageSize: Int,
    val sessionId: String?,
) : PageQuery(page, pageSize)
