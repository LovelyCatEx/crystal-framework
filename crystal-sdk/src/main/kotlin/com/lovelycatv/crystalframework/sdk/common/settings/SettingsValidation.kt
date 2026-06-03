package com.lovelycatv.crystalframework.sdk.common.settings

import com.lovelycatv.crystalframework.sdk.common.settings.types.SettingsItemDeclaration
import com.lovelycatv.crystalframework.sdk.common.settings.types.SettingsItemValueType
import com.lovelycatv.crystalframework.shared.utils.parseObject

fun SettingsItemDeclaration.validateConfigValue(value: String): SettingsValidationResult {
    var errorMessage: String? = null
    val pass = try {
        when (this.valueType) {
            SettingsItemValueType.STRING -> value
            SettingsItemValueType.NUMBER -> value.toLongOrNull()
            SettingsItemValueType.DECIMAL -> value.toDoubleOrNull()
            SettingsItemValueType.BOOLEAN -> value.toBooleanStrictOrNull()
            SettingsItemValueType.ENUM_SINGLE -> {
                val enumValues = this.enumValues
                    ?: throw IllegalArgumentException("Declaration ${this.key} does not have enum values")
                value in enumValues
            }
            SettingsItemValueType.ENUM_MULTIPLE -> {
                val enumValues = this.enumValues
                    ?: throw IllegalArgumentException("Declaration ${this.key} does not have enum values")
                val parsed = value.parseObject<List<String>>()
                parsed.all { it in enumValues }
            }
        } != null
    } catch (e: Exception) {
        errorMessage = e.localizedMessage ?: e.message ?: "${this.key} = $value"
        false
    }
    return SettingsValidationResult(pass, errorMessage)
}
