package com.lovelycatv.crystalframework.sdk.common.settings

import com.lovelycatv.crystalframework.sdk.common.settings.types.SettingsItemDeclaration
import com.lovelycatv.crystalframework.sdk.common.settings.types.SettingsItemValueType
import com.lovelycatv.crystalframework.shared.utils.parseObject

/**
 * Converts a raw config string into its typed value, or returns null when the raw
 * value is not a well-formed representation of this type.
 *
 * Array types are transported / stored as a JSON array string (e.g. `["1","2"]`);
 * each element is parsed and converted with the same predicate as its scalar
 * counterpart. Enum membership is NOT validated here — see [validateConfigValue].
 */
fun SettingsItemValueType.convertValue(raw: String): Any? = try {
    when (this) {
        SettingsItemValueType.STRING -> raw
        SettingsItemValueType.NUMBER -> raw.toLongOrNull()
        SettingsItemValueType.DECIMAL -> raw.toDoubleOrNull()
        SettingsItemValueType.BOOLEAN -> raw.toBooleanStrictOrNull()
        SettingsItemValueType.ENUM_SINGLE -> raw
        SettingsItemValueType.ENUM_MULTIPLE -> raw.parseObject<List<*>>().map { it.toString() }
        SettingsItemValueType.STRING_ARRAY -> raw.parseObject<List<*>>().map { it.toString() }
        SettingsItemValueType.NUMBER_ARRAY -> raw.parseObject<List<*>>().map { it.toString().toLong() }
        SettingsItemValueType.DECIMAL_ARRAY -> raw.parseObject<List<*>>().map { it.toString().toDouble() }
        SettingsItemValueType.BOOLEAN_ARRAY -> raw.parseObject<List<*>>().map {
            it.toString().toBooleanStrictOrNull() ?: throw IllegalArgumentException("'$it' is not a boolean")
        }
    }
} catch (e: Exception) {
    null
}

/**
 * Format-level type check (no enum membership). A value matches when it can be
 * converted to its typed representation.
 */
fun SettingsItemValueType.matches(raw: String): Boolean = this.convertValue(raw) != null

fun SettingsItemDeclaration.validateConfigValue(value: String): SettingsValidationResult {
    var errorMessage: String? = null
    val pass = try {
        when (this.valueType) {
            SettingsItemValueType.ENUM_SINGLE -> {
                val enumValues = this.enumValues
                    ?: throw IllegalArgumentException("Declaration ${this.key} does not have enum values")
                value in enumValues
            }
            SettingsItemValueType.ENUM_MULTIPLE -> {
                val enumValues = this.enumValues
                    ?: throw IllegalArgumentException("Declaration ${this.key} does not have enum values")
                value.parseObject<List<String>>().all { it in enumValues }
            }
            else -> this.valueType.convertValue(value)
        } != null
    } catch (e: Exception) {
        errorMessage = e.localizedMessage ?: e.message ?: "${this.key} = $value"
        false
    }
    return SettingsValidationResult(pass, errorMessage)
}
