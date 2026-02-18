package com.lovelycatv.template.springboot.system.service.impl

import com.lovelycatv.template.springboot.shared.utils.SnowIdGenerator
import com.lovelycatv.template.springboot.system.entity.SystemSettingsEntity
import com.lovelycatv.template.springboot.system.repository.SystemSettingsRepository
import com.lovelycatv.template.springboot.system.service.SystemSettingsService
import com.lovelycatv.template.springboot.system.types.SystemSettings
import com.lovelycatv.template.springboot.system.types.SystemSettingsConstants
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Service
import kotlin.toString

@Service
class SystemSettingsServiceImpl(
    private val systemSettingsRepository: SystemSettingsRepository,
    private val snowIdGenerator: SnowIdGenerator
) : SystemSettingsService {
    private val logger = logger()
    private var cachedSystemSettings: SystemSettings? = null

    override fun getRepository(): SystemSettingsRepository {
        return this.systemSettingsRepository
    }

    override fun refreshSystemSettings() {
        this.cachedSystemSettings = null
    }

    override suspend fun getSystemSettings(): SystemSettings {
        return cachedSystemSettings ?: SystemSettings(
            mail = SystemSettings.Mail(
                smtp = SystemSettings.Mail.SMTP(
                    host = getSettings(SystemSettingsConstants.Mail.SMTP.HOST) { "127.0.0.1" } !!,
                    port = getSettings(SystemSettingsConstants.Mail.SMTP.PORT) { "465" }!!.toInt(),
                    username = getSettings(SystemSettingsConstants.Mail.SMTP.USERNAME) { "username" }!!,
                    password = getSettings(SystemSettingsConstants.Mail.SMTP.PASSWORD) { "password" }!!,
                    ssl = getSettings(SystemSettingsConstants.Mail.SMTP.SSL) { "true" }!!.toBoolean(),
                    fromEmail = getSettings(SystemSettingsConstants.Mail.SMTP.FROM_EMAIL) { "user@example.com" }!!,
                )
            )
        ).also {
            this.cachedSystemSettings = it
        }
    }

    override suspend fun updateSystemSettings(settings: SystemSettings) {
        setSettings(SystemSettingsConstants.Mail.SMTP.HOST, settings.mail.smtp.host)
        setSettings(SystemSettingsConstants.Mail.SMTP.PORT, settings.mail.smtp.port.toString())
        setSettings(SystemSettingsConstants.Mail.SMTP.USERNAME, settings.mail.smtp.username)
        setSettings(SystemSettingsConstants.Mail.SMTP.PASSWORD, settings.mail.smtp.password)
        setSettings(SystemSettingsConstants.Mail.SMTP.SSL, settings.mail.smtp.ssl.toString())
        setSettings(SystemSettingsConstants.Mail.SMTP.FROM_EMAIL, settings.mail.smtp.fromEmail)

        this.refreshSystemSettings()
    }

    override suspend fun getSettings(key: String): SystemSettingsEntity? {
        return this.getRepository()
            .findByConfigKey(key)
            .awaitFirstOrNull()
    }

    override suspend fun setSettings(key: String, value: String?) {
        val existing = this.getSettings(key)

        if (existing != null) {
            // internalUpdate
            this.getRepository().save(
                existing.apply {
                    this.configValue = value
                }
            ).awaitFirstOrNull()

            logger.info("System settings $key updated to ${existing.configValue}")
        } else {
            // insert
            this.getRepository().save(
                SystemSettingsEntity(
                    id = snowIdGenerator.nextId(),
                    configKey = key,
                    configValue = value
                ) newEntity true
            ).awaitFirstOrNull()

            logger.info("System settings $key saved, value: $value")
        }
    }

    override suspend fun getSettings(
        key: String,
        absentValue: (absentOrNull: Boolean) -> String?
    ): String? {
        val existing = this.getSettings(key)

        return if (existing != null) {
            if (existing.configValue != null) {
                existing.configValue
            } else {
                val newValue = absentValue.invoke(false)

                this.setSettings(key, newValue)

                newValue
            }
        } else {
            val newValue = absentValue.invoke(true)

            this.setSettings(key, newValue)

            newValue
        }
    }
}