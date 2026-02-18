/*
 * Copyright 2026 lovelycat
 *
 * Use of this source code is governed by the Apache License, Version 2.0,
 * that can be found in the LICENSE file.
 *
 */

package com.lovelycatv.template.springboot.shared.request

data class PaginatedResponseData<T>(
    val page: Int,
    val pageSize: Int,
    val total: Long,
    val totalPages: Int,
    val records: List<T>,
)
