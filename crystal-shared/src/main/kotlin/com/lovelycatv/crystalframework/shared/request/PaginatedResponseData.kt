/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.shared.request

data class PaginatedResponseData<T>(
    val page: Int,
    val pageSize: Int,
    val total: Long,
    val totalPages: Int,
    val records: List<T>,
)
