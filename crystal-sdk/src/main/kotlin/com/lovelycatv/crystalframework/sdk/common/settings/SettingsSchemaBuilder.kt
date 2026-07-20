package com.lovelycatv.crystalframework.sdk.common.settings

import com.lovelycatv.crystalframework.sdk.common.settings.types.SettingsItemDeclaration

private const val SECRET_MASK = "***"

suspend fun buildSettingsSchemaResponse(
    declarations: List<SettingsItemDeclaration>,
    valueResolver: suspend (key: String) -> String?,
): Map<String, Any?> {
    val mapping = declarations.associate { d ->
        val resolvedValue = valueResolver(d.key) ?: d.defaultValue
        val exposedValue = if (d.isSecret && !resolvedValue.isNullOrBlank()) SECRET_MASK else resolvedValue
        val exposedDefaultValue = if (d.isSecret && !d.defaultValue.isNullOrBlank()) SECRET_MASK else d.defaultValue

        d.key to mapOf(
            "sort" to d.sort,
            "valueType" to d.valueType.name,
            "value" to exposedValue,
            "defaultValue" to exposedDefaultValue,
            "enumValues" to d.enumValues,
            "isSecret" to d.isSecret,
            "tab" to d.key.takeIf { "." in it }?.substringBefore("."),
            "group" to d.key.takeIf { "." in it }?.split(".")?.dropLast(1)?.joinToString("."),
        )
    }
    return mapOf(
        "groups" to mapping.values.mapNotNull { it["group"] }.distinct(),
        "items" to mapping,
    )
}
