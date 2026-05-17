package com.lovelycatv.crystalframework.shared.controller.dto

import com.lovelycatv.crystalframework.shared.request.PageQuery

open class BaseManagerReadDTO(
    override val page: Int,
    override val pageSize: Int,
    open val id: Long? = null,
    open val searchKeyword: String? = null,
    open val startTime: Long? = null,
    open val endTime: Long? = null,
) : PageQuery(page, pageSize)