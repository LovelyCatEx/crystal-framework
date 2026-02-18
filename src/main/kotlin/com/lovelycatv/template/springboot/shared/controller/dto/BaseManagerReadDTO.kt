package com.lovelycatv.template.springboot.shared.controller.dto

import com.lovelycatv.template.springboot.shared.request.PageQuery

open class BaseManagerReadDTO(
    override val page: Int,
    override val pageSize: Int,
    open val id: Long? = null,
    open val searchKeyword: String? = null,
) : PageQuery(page, pageSize)