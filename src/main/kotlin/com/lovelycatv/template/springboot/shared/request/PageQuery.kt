package com.lovelycatv.template.springboot.shared.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

open class PageQuery(
    @field:Min(1, message = "Page index must be greater than or equal to 1")
    val page: Int,
    @field:Min(value = 0, message = "Page size must be between 1 and 20")
    @field:Max(20, message = "Page size must be between 1 and 20")
    val pageSize: Int
)
