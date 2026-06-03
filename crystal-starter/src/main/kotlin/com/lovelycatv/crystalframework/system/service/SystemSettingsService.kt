package com.lovelycatv.crystalframework.system.service

import com.lovelycatv.crystalframework.sdk.common.settings.convertValue
import com.lovelycatv.crystalframework.sdk.common.settings.types.SettingsItemDeclaration
import com.lovelycatv.crystalframework.shared.service.CachedBaseService
import com.lovelycatv.crystalframework.shared.types.system.SystemSettings
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
        declaration: SettingsItemDeclaration
    ): R? {
        val settingsValue = getSettings(declaration.key) { declaration.defaultValue }
            ?: return null

        return declaration.valueType.convertValue(settingsValue) as? R?
    }

    suspend fun setSettings(key: String, value: String?)

    suspend fun setSettings(declaration: SettingsItemDeclaration, value: String?) {
        this.setSettings(declaration.key, value)
    }
}