package com.lovelycatv.crystalframework.sdk.common.settings.types

data class SettingsItemDeclaration(
    val key: String,
    val valueType: SettingsItemValueType,
    val defaultValue: String? = null,
    val sort: Int = 0,
    val enumValues: List<String>? = null,
    val isSecret: Boolean = false,
)
