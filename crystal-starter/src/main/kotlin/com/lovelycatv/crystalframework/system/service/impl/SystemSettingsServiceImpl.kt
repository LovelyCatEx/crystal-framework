package com.lovelycatv.crystalframework.system.service.impl

import com.lovelycatv.crystalframework.sdk.system.settings.SystemSettingsRegistry
import com.lovelycatv.crystalframework.sdk.common.settings.types.SettingsItemValueType
import com.lovelycatv.crystalframework.shared.constants.RedisConstants
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.types.encrypt.ApiEncryptionScope
import com.lovelycatv.crystalframework.shared.types.system.SystemSettings
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.system.entity.SystemSettingsEntity
import com.lovelycatv.crystalframework.system.repository.SystemSettingsRepository
import com.lovelycatv.crystalframework.system.service.SystemSettingsService
import com.lovelycatv.crystalframework.system.types.SystemSettingsConstants
import com.lovelycatv.vertex.cache.store.ExpiringKVStore
import com.lovelycatv.vertex.log.logger
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer
import org.springframework.stereotype.Service
import java.util.*
import kotlin.reflect.KClass

@Service
class SystemSettingsServiceImpl(
    private val systemSettingsRepository: SystemSettingsRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val redisService: RedisService,
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, Any>,
    private val redisMessageListenerContainer: ReactiveRedisMessageListenerContainer,
    private val systemSettingsRegistry: SystemSettingsRegistry,
    override val eventPublisher: ApplicationEventPublisher,
) : SystemSettingsService {
    private val logger = logger()

    @Volatile
    private var cachedSystemSettings: SystemSettings? = null

    private val instanceId = UUID.randomUUID().toString()

    private val refreshTopic = ChannelTopic(RedisConstants.SYSTEM_SETTINGS_REFRESH_TOPIC)

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        coroutineScope.launch {
            getSystemSettings()
        }
    }

    @PostConstruct
    fun subscribeRefreshTopic() {
        redisMessageListenerContainer
            .receive(refreshTopic)
            .subscribe { message ->
                val sender = message.message
                if (sender == instanceId) {
                    return@subscribe
                }
                logger.info("Received system settings refresh signal from instance $sender")
                cachedSystemSettings = null
            }
    }

    override fun getRepository(): SystemSettingsRepository {
        return this.systemSettingsRepository
    }

    override val cacheStore: ExpiringKVStore<String, SystemSettingsEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<SystemSettingsEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<SystemSettingsEntity> = SystemSettingsEntity::class

    override fun refreshSystemSettings() {
        this.cachedSystemSettings = null

        this.syncToCacheAsync()

        reactiveRedisTemplate
            .convertAndSend(refreshTopic.topic, instanceId)
            .subscribe()

    }

    override suspend fun getSystemSettings(): SystemSettings {
        return cachedSystemSettings ?: SystemSettings(
            basic = getSystemBasicSettings(),
            bootstrap = getSystemBootstrapSettings(),
            mail = getSystemMailSettings(),
            messageChannel = getSystemMessageChannelSettings(),
            security = getSystemSecuritySettings(),
        ).also {
            this.cachedSystemSettings = it
            this.syncToCacheAsync()
        }
    }


    override suspend fun getSystemBasicSettings(): SystemSettings.Basic {
        return SystemSettings.Basic(
            baseUrl = getSettings(SystemSettingsConstants.Basic.BASE_URL)!!,
            waterMark = SystemSettings.Basic.WaterMark(
                enabled = getSettings(SystemSettingsConstants.Basic.WaterMark.ENABLED)!!,
                type = getSettings(SystemSettingsConstants.Basic.WaterMark.TYPE)!!,
                customValue = getSettings(SystemSettingsConstants.Basic.WaterMark.CUSTOM_VALUE)!!,
                fontColor = getSettings(SystemSettingsConstants.Basic.WaterMark.FONT_COLOR)!!
            )
        )
    }

    override suspend fun getSystemBootstrapSettings(): SystemSettings.Bootstrap {
        return SystemSettings.Bootstrap(
            autoCheckRbacTableData = getSettings(SystemSettingsConstants.Bootstrap.AUTO_CHECK_RBAC_TABLE_DATA)!!
        )
    }

    override suspend fun getSystemMailSettings(): SystemSettings.Mail {
        return SystemSettings.Mail(
            smtp = SystemSettings.Mail.SMTP(
                host = getSettings(SystemSettingsConstants.Mail.SMTP.HOST)!!,
                port = getSettings<Long>(SystemSettingsConstants.Mail.SMTP.PORT)!!.toInt(),
                username = getSettings(SystemSettingsConstants.Mail.SMTP.USERNAME)!!,
                password = getSettings(SystemSettingsConstants.Mail.SMTP.PASSWORD)!!,
                ssl = getSettings(SystemSettingsConstants.Mail.SMTP.SSL)!!,
                fromEmail = getSettings(SystemSettingsConstants.Mail.SMTP.FROM_EMAIL)!!,
            )
        )
    }

    override suspend fun getSystemMessageChannelSettings(): SystemSettings.MessageChannel {
        return SystemSettings.MessageChannel(
            lark = SystemSettings.MessageChannel.Lark(
                appId = getSettings(SystemSettingsConstants.MessageChannel.Lark.APP_ID)!!,
                appSecret = getSettings(SystemSettingsConstants.MessageChannel.Lark.APP_SECRET)!!,
                baseUrl = getSettings(SystemSettingsConstants.MessageChannel.Lark.BASE_URL)!!,
            )
        )
    }

    override suspend fun getSystemSecuritySettings(): SystemSettings.Security {
        return SystemSettings.Security(
            api = SystemSettings.Security.Api(
                encrypt = SystemSettings.Security.Api.Encrypt(
                    enabled = getSettings(SystemSettingsConstants.Security.Api.Encrypt.ENABLE)!! ,
                    scope = ApiEncryptionScope.valueOf(
                        getSettings<String>(SystemSettingsConstants.Security.Api.Encrypt.SCOPE)!!
                    ),
                    securityLevel = getSettings<Long>(SystemSettingsConstants.Security.Api.Encrypt.SECURITY_LEVEL)!!.toInt(),
                )
            )
        )
    }

    override suspend fun updateSystemSettings(settings: SystemSettings) {
        setSettings(SystemSettingsConstants.Basic.BASE_URL, settings.basic.baseUrl)
        setSettings(SystemSettingsConstants.Basic.WaterMark.ENABLED, settings.basic.waterMark.enabled.toString())
        setSettings(SystemSettingsConstants.Basic.WaterMark.TYPE, settings.basic.waterMark.type)
        setSettings(SystemSettingsConstants.Basic.WaterMark.CUSTOM_VALUE, settings.basic.waterMark.customValue)
        setSettings(SystemSettingsConstants.Basic.WaterMark.FONT_COLOR, settings.basic.waterMark.fontColor)

        setSettings(SystemSettingsConstants.Bootstrap.AUTO_CHECK_RBAC_TABLE_DATA, settings.bootstrap.autoCheckRbacTableData.toString())

        setSettings(SystemSettingsConstants.Mail.SMTP.HOST, settings.mail.smtp.host)
        setSettings(SystemSettingsConstants.Mail.SMTP.PORT, settings.mail.smtp.port.toString())
        setSettings(SystemSettingsConstants.Mail.SMTP.USERNAME, settings.mail.smtp.username)
        setSettings(SystemSettingsConstants.Mail.SMTP.PASSWORD, settings.mail.smtp.password)
        setSettings(SystemSettingsConstants.Mail.SMTP.SSL, settings.mail.smtp.ssl.toString())
        setSettings(SystemSettingsConstants.Mail.SMTP.FROM_EMAIL, settings.mail.smtp.fromEmail)

        setSettings(SystemSettingsConstants.MessageChannel.Lark.APP_ID, settings.messageChannel.lark.appId)
        setSettings(SystemSettingsConstants.MessageChannel.Lark.APP_SECRET, settings.messageChannel.lark.appSecret)
        setSettings(SystemSettingsConstants.MessageChannel.Lark.BASE_URL, settings.messageChannel.lark.baseUrl)

        setSettings(SystemSettingsConstants.Security.Api.Encrypt.ENABLE, settings.security.api.encrypt.enabled.toString())
        setSettings(SystemSettingsConstants.Security.Api.Encrypt.SCOPE, settings.security.api.encrypt.scope.name)
        setSettings(SystemSettingsConstants.Security.Api.Encrypt.SECURITY_LEVEL, settings.security.api.encrypt.securityLevel.toString())

        this.refreshSystemSettings()
    }

    override suspend fun updateSystemSettings(settings: Map<String, String?>) {
        val declarationsByKey = systemSettingsRegistry.declarationMap()

        settings.forEach { (key, value) ->
            val declaration = declarationsByKey[key]
                ?: throw BusinessException("setting key '$key' is not declared")
            if (value != null && !declaration.valueType.matches(value)) {
                throw BusinessException(
                    "setting '$key' expects ${declaration.valueType.name.lowercase()} but got '$value'"
                )
            }
        }

        settings.forEach { (key, value) ->
            this.setSettings(key, value)
        }

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

    private fun syncToCacheAsync() {
        coroutineScope.launch {
            this@SystemSettingsServiceImpl.syncToCache()
        }
    }

    /**
     * As some module could not access the system module directly.
     *
     * Some settings required by other modules will be shared in cache.
     */
    private suspend fun syncToCache() {
        val settings = getSystemSettings()

        redisService.set(
            RedisConstants.SYSTEM_SETTINGS,
            settings
        )

        logger.info("System settings synchronized to cache")
    }

    private fun SettingsItemValueType.matches(raw: String): Boolean = when (this) {
        SettingsItemValueType.STRING -> true
        SettingsItemValueType.NUMBER -> raw.toLongOrNull() != null
        SettingsItemValueType.DECIMAL -> raw.toDoubleOrNull() != null
        SettingsItemValueType.BOOLEAN -> raw.toBooleanStrictOrNull() != null
        SettingsItemValueType.ENUM_SINGLE -> true
        SettingsItemValueType.ENUM_MULTIPLE -> true
    }
}