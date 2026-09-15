/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.utils

import com.lovelycatv.crystalframework.shared.request.PageQuery
import com.lovelycatv.crystalframework.shared.request.PaginatedResponseData
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