package com.lovelycatv.crystalframework.tenant.settings.service

import com.lovelycatv.crystalframework.sdk.system.settings.types.SystemSettingsItemDeclaration
import com.lovelycatv.crystalframework.sdk.system.settings.types.SystemSettingsItemValueType
import com.lovelycatv.crystalframework.shared.service.CachedBaseService
import com.lovelycatv.crystalframework.shared.utils.parseObject
import com.lovelycatv.crystalframework.tenant.settings.entity.TenantSettingsEntity
import com.lovelycatv.crystalframework.tenant.settings.repository.TenantSettingsRepository
import com.lovelycatv.crystalframework.tenant.settings.types.TenantSettingsView

interface TenantSettingsService : CachedBaseService<TenantSettingsRepository, TenantSettingsEntity> {
    fun refreshTenantSettings(tenantId: Long)

    suspend fun getTenantSettings(tenantId: Long): TenantSettingsView

    suspend fun updateTenantSettings(tenantId: Long, settings: Map<String, String?>)

    suspend fun getSettings(tenantId: Long, key: String): TenantSettingsEntity?

    suspend fun getSettings(
        tenantId: Long,
        key: String,
        absentValue: (absentOrNull: Boolean) -> String?,
    ): String?

    @Suppress("UNCHECKED_CAST")
    suspend fun <R> getSettings(
        tenantId: Long,
        declaration: SystemSettingsItemDeclaration,
    ): R? {
        val settingsValue = getSettings(tenantId, declaration.key) { declaration.defaultValue }
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

    suspend fun setSettings(tenantId: Long, key: String, value: String?)

    suspend fun setSettings(
        tenantId: Long,
        declaration: SystemSettingsItemDeclaration,
        value: String?,
    ) {
        this.setSettings(tenantId, declaration.key, value)
    }
}
