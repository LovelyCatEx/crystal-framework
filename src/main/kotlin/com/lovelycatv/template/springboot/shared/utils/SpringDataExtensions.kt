/*
 * Copyright 2026 lovelycat
 *
 * Use of this source code is governed by the Apache License, Version 2.0,
 * that can be found in the LICENSE file.
 *
 */

package com.lovelycatv.template.springboot.shared.utils

import com.lovelycatv.template.springboot.shared.request.PageQuery
import com.lovelycatv.template.springboot.shared.request.PaginatedResponseData
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import kotlin.math.ceil

class SpringDataExtensions private constructor()

fun <T: Any> Page<T>.toPaginatedResponseData(): PaginatedResponseData<T> {
    return this.toPaginatedResponseData { it }
}

fun <T: Any, R> Page<T>.toPaginatedResponseData(recordTransform: (T) -> R): PaginatedResponseData<R> {
    return PaginatedResponseData(
        page = this.pageable.pageNumber,
        pageSize = this.pageable.pageSize,
        total = this.totalElements,
        totalPages = this.totalPages,
        records = this.toList().map { recordTransform(it) }
    )
}

fun <T> PageQuery.toPaginatedResponseData(
    total: Long = 0,
    records: List<T> = emptyList()
): PaginatedResponseData<T> {
    return PaginatedResponseData(
        page = this.page,
        pageSize = this.pageSize,
        total = total,
        totalPages = ceil(total.toDouble() / this.pageSize).toInt(),
        records = records
    )
}

fun PageQuery.toPageable(sortDirection: Sort.Direction? = null, vararg sortColumns: String): PageRequest {
    return if (sortDirection == null || sortColumns.isEmpty()) {
        PageRequest
            .of(
                this.page - 1,
                this.pageSize
            )
    } else {
        PageRequest
            .of(
                this.page - 1,
                this.pageSize,
                sortDirection,
                *sortColumns
            )
    }
}