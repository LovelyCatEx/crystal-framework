package com.lovelycatv.template.springboot.system.service

import com.lovelycatv.template.springboot.shared.service.BaseService
import com.lovelycatv.template.springboot.system.entity.SystemSettingsEntity
import com.lovelycatv.template.springboot.system.repository.SystemSettingsRepository
import com.lovelycatv.template.springboot.system.types.SystemSettings

interface SystemSettingsService : BaseService<SystemSettingsRepository, SystemSettingsEntity> {
    fun refreshSystemSettings()

    suspend fun getSystemSettings(): SystemSettings

    suspend fun updateSystemSettings(settings: SystemSettings)

    suspend fun getSettings(key: String): SystemSettingsEntity?

    suspend fun getSettings(key: String, absentValue: (absentOrNull: Boolean) -> String?): String?

    suspend fun setSettings(key: String, value: String?)
}