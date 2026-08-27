package com.lovelycatv.crystalframework.sdk.common.settings.types

/**
 * Contract between a settings owner and the schema/translation layer.
 *
 * [enumTypeKey] names the shared enum whose values populate [enumValues]. When set, the
 * frontend resolves each option's label via `enums.<enumTypeKey>.<value>` in the locale
 * file — one translation table shared by every setting that reuses the enum — instead of
 * repeating the same 5 values under every setting key. Leave `null` for ad-hoc option
 * lists that don't correspond to a shared enum (falls back to `enums.<settingsKey>.<value>`).
 */
data class SettingsItemDeclaration(
    val key: String,
    val valueType: SettingsItemValueType,
    val defaultValue: String? = null,
    val sort: Int = 0,
    val enumValues: List<String>? = null,
    val isSecret: Boolean = false,
    val enumTypeKey: String? = null,
)
