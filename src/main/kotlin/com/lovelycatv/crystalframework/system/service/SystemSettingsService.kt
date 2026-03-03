package com.lovelycatv.crystalframework.system.service

import com.lovelycatv.crystalframework.cache.service.CachedBaseService
import com.lovelycatv.crystalframework.system.entity.SystemSettingsEntity
import com.lovelycatv.crystalframework.system.repository.SystemSettingsRepository
import com.lovelycatv.crystalframework.system.types.SystemSettings
import com.lovelycatv.crystalframework.system.types.SystemSettingsItemDeclaration
import com.lovelycatv.crystalframework.system.types.SystemSettingsItemValueType

interface SystemSettingsService : CachedBaseService<SystemSettingsRepository, SystemSettingsEntity> {
    fun refreshSystemSettings()

    suspend fun getSystemSettings(): SystemSettings

    suspend fun updateSystemSettings(settings: SystemSettings)

    suspend fun getSettings(key: String): SystemSettingsEntity?

    suspend fun getSettings(key: String, absentValue: (absentOrNull: Boolean) -> String?): String?

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
        } as? R?
    }

    suspend fun setSettings(key: String, value: String?)

    suspend fun setSettings(declaration: SystemSettingsItemDeclaration, value: String?) {
        this.setSettings(declaration.key, value)
    }
}