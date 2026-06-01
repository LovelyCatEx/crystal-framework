package com.lovelycatv.crystalframework.system.service

import com.lovelycatv.crystalframework.sdk.system.settings.types.SystemSettingsItemDeclaration
import com.lovelycatv.crystalframework.sdk.system.settings.types.SystemSettingsItemValueType
import com.lovelycatv.crystalframework.shared.service.CachedBaseService
import com.lovelycatv.crystalframework.shared.types.system.SystemSettings
import com.lovelycatv.crystalframework.shared.utils.parseObject
import com.lovelycatv.crystalframework.system.entity.SystemSettingsEntity
import com.lovelycatv.crystalframework.system.repository.SystemSettingsRepository

interface SystemSettingsService : CachedBaseService<SystemSettingsRepository, SystemSettingsEntity> {
    fun refreshSystemSettings()

    suspend fun getSystemSettings(): SystemSettings

    suspend fun getSystemBasicSettings(): SystemSettings.Basic

    suspend fun getSystemBootstrapSettings(): SystemSettings.Bootstrap

    suspend fun getSystemMailSettings(): SystemSettings.Mail

    suspend fun getSystemMessageChannelSettings(): SystemSettings.MessageChannel

    suspend fun getSystemSecuritySettings(): SystemSettings.Security

    suspend fun updateSystemSettings(settings: SystemSettings)

    suspend fun updateSystemSettings(settings: Map<String, String?>)

    suspend fun getSettings(key: String): SystemSettingsEntity?

    suspend fun getSettings(key: String, absentValue: (absentOrNull: Boolean) -> String?): String?

    @Suppress("UNCHECKED_CAST")
    suspend fun <R> getSettings(
        declaration: SystemSettingsItemDeclaration
    ): R? {
        val settingsValue = getSettings(declaration.key) { declaration.defaultValue }
            ?: return null

        return when (declaration.valueType) {
            SystemSettingsItemValueType.STRING -> settingsValue
            SystemSettingsItemValueType.NUMBER -> settingsValue.toLongOrNull()
            SystemSettingsItemValueType.DECIMAL -> settingsValue.toDoubleOrNull()
            SystemSettingsItemValueType.BOOLEAN -> settingsValue.toBooleanStrictOrNull()
            SystemSettingsItemValueType.ENUM_SINGLE -> settingsValue
            SystemSettingsItemValueType.ENUM_MULTIPLE -> settingsValue.parseObject<List<String>>()
        } as? R?
    }

    suspend fun setSettings(key: String, value: String?)

    suspend fun setSettings(declaration: SystemSettingsItemDeclaration, value: String?) {
        this.setSettings(declaration.key, value)
    }
}