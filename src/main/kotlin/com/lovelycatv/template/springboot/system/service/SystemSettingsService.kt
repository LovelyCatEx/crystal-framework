package com.lovelycatv.template.springboot.system.service

import com.lovelycatv.template.springboot.shared.service.BaseService
import com.lovelycatv.template.springboot.system.entity.SystemSettingsEntity
import com.lovelycatv.template.springboot.system.repository.SystemSettingsRepository
import com.lovelycatv.template.springboot.system.types.SystemSettings
import com.lovelycatv.template.springboot.system.types.SystemSettingsItemDeclaration
import com.lovelycatv.template.springboot.system.types.SystemSettingsItemValueType

interface SystemSettingsService : BaseService<SystemSettingsRepository, SystemSettingsEntity> {
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