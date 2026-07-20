package com.lovelycatv.crystalframework.sdk.common.settings

import com.lovelycatv.crystalframework.sdk.common.settings.types.SettingsItemDeclaration

suspend fun buildSettingsSchemaResponse(
    declarations: List<SettingsItemDeclaration>,
    valueResolver: suspend (key: String) -> String?,
): Map<String, Any?> {
    val mapping = declarations.associate { d ->
        val resolvedValue = valueResolver(d.key) ?: d.defaultValue
        val hasValue = d.isSecret && !resolvedValue.isNullOrBlank()
        val exposedValue = if (d.isSecret) null else resolvedValue
        val exposedDefaultValue = if (d.isSecret) null else d.defaultValue

        d.key to mapOf(
            "sort" to d.sort,
            "valueType" to d.valueType.name,
            "value" to exposedValue,
            "defaultValue" to exposedDefaultValue,
            "enumValues" to d.enumValues,
            "isSecret" to d.isSecret,
            "hasValue" to hasValue,
            "tab" to d.key.takeIf { "." in it }?.substringBefore("."),
            "group" to d.key.takeIf { "." in it }?.split(".")?.dropLast(1)?.joinToString("."),
        )
    }
    return mapOf(
        "groups" to mapping.values.mapNotNull { it["group"] }.distinct(),
        "items" to mapping,
    )
}
