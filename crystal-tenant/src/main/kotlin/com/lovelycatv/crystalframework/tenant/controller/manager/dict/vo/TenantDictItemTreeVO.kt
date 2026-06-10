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
