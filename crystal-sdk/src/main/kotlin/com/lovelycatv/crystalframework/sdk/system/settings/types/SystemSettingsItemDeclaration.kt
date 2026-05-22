package com.lovelycatv.crystalframework.sdk.system.settings.types

data class SystemSettingsItemDeclaration(
    val key: String,
    val valueType: SystemSettingsItemValueType,
    val defaultValue: String? = null,
    val sort: Int = 0,
    val enumValues: List<String>? = null,
)
