/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.tenant.controller.manager.dict.vo

data class TenantDictItemTreeVO(
    val id: String,
    val itemCode: String,
    val itemValue: String,
    val parentId: String?,
    val sortOrder: Int,
    val isDefault: Boolean,
    val status: Int,
    val createdTime: String,
    val modifiedTime: String,
    val children: List<TenantDictItemTreeVO>
)
