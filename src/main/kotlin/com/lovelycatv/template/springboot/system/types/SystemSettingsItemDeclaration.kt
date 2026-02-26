package com.lovelycatv.template.springboot.system.types

data class SystemSettingsItemDeclaration(
    val key: String,
    val valueType: SystemSettingsItemValueType,
    val defaultValue: String? = null
)