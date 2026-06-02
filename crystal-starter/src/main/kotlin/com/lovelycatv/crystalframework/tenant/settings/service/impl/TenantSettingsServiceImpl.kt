package com.lovelycatv.crystalframework.tenant.settings.service.impl

import com.lovelycatv.crystalframework.sdk.system.settings.types.SystemSettingsItemValueType
import com.lovelycatv.crystalframework.sdk.tenant.settings.TenantSettingsRegistry
import com.lovelycatv.crystalframework.shared.constants.RedisConstants
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.tenant.settings.entity.TenantSettingsEntity
import com.lovelycatv.crystalframework.tenant.settings.repository.TenantSettingsRepository
import com.lovelycatv.crystalframework.tenant.settings.service.TenantSettingsService
import com.lovelycatv.crystalframework.tenant.settings.types.TenantSettingsConstants
import com.lovelycatv.crystalframework.tenant.settings.types.TenantSettingsView
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
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

@Service
class TenantSettingsServiceImpl(
    private val tenantSettingsRepository: TenantSettingsRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val redisService: RedisService,
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, Any>,
    private val redisMessageListenerContainer: ReactiveRedisMessageListenerContainer,
    private val tenantSettingsRegistry: TenantSettingsRegistry,
    override val eventPublisher: ApplicationEventPublisher,
) : TenantSettingsService {
    private val logger = logger()

    private val cachedTenantSettings = ConcurrentHashMap<Long, TenantSettingsView>()

    private val instanceId = UUID.randomUUID().toString()

    private val refreshTopic = ChannelTopic(RedisConstants.TENANT_SETTINGS_REFRESH_TOPIC)

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    @PostConstruct
    fun subscribeRefreshTopic() {
        redisMessageListenerContainer
            .receive(refreshTopic)
            .subscribe { message ->
                val payload = message.message
                val parts = payload.split(REFRESH_PAYLOAD_DELIMITER, limit = 2)
                if (parts.size != 2) {
                    return@subscribe
                }
                val sender = parts[0]
                if (sender == instanceId) {
                    return@subscribe
                }
                val tenantId = parts[1].toLongOrNull() ?: return@subscribe
                logger.info("Received tenant($tenantId) settings refresh signal from instance $sender")
                cachedTenantSettings.remove(tenantId)
            }
    }

    override fun getRepository(): TenantSettingsRepository {
        return this.tenantSettingsRepository
    }

    override val cacheStore: ExpiringKVStore<String, TenantSettingsEntity>
        get() = redisService.asKVStore()
    override val listCacheStore: ExpiringKVStore<String, List<TenantSettingsEntity>>
        get() = redisService.asKVStore()
    override val entityClass: KClass<TenantSettingsEntity> = TenantSettingsEntity::class

    override fun refreshTenantSettings(tenantId: Long) {
        this.cachedTenantSettings.remove(tenantId)

        this.syncToCacheAsync(tenantId)

        reactiveRedisTemplate
            .convertAndSend(
                refreshTopic.topic,
                "$instanceId$REFRESH_PAYLOAD_DELIMITER$tenantId"
            )
            .subscribe()
    }

    override suspend fun getTenantSettings(tenantId: Long): TenantSettingsView {
        return cachedTenantSettings[tenantId] ?: TenantSettingsView(
            notification = TenantSettingsView.Notification(
                memberJoinNotifyEmail = getSettings(tenantId, TenantSettingsConstants.Notification.MEMBER_JOIN_NOTIFY_EMAIL)!!,
            ),
        ).also {
            this.cachedTenantSettings[tenantId] = it
            this.syncToCacheAsync(tenantId)
        }
    }

    override suspend fun updateTenantSettings(tenantId: Long, settings: Map<String, String?>) {
        val declarationsByKey = tenantSettingsRegistry.declarationMap()

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
            this.setSettings(tenantId, key, value)
        }

        this.refreshTenantSettings(tenantId)
    }

    override suspend fun getSettings(tenantId: Long, key: String): TenantSettingsEntity? {
        return this.getRepository()
            .findByTenantIdAndConfigKey(tenantId, key)
            .awaitFirstOrNull()
    }

    override suspend fun setSettings(tenantId: Long, key: String, value: String?) {
        val existing = this.getSettings(tenantId, key)

        if (existing != null) {
            this.getRepository()
                .save(existing.apply { this.configValue = value })
                .awaitFirstOrNull()

            logger.info("Tenant($tenantId) settings $key updated to ${existing.configValue}")
        } else {
            this.getRepository().save(
                TenantSettingsEntity(
                    id = snowIdGenerator.nextId(),
                    tenantId = tenantId,
                    configKey = key,
                    configValue = value,
                ) newEntity true
            ).awaitFirstOrNull()

            logger.info("Tenant($tenantId) settings $key saved, value: $value")
        }
    }

    override suspend fun getSettings(
        tenantId: Long,
        key: String,
        absentValue: (absentOrNull: Boolean) -> String?,
    ): String? {
        val existing = this.getSettings(tenantId, key)

        return if (existing != null) {
            if (existing.configValue != null) {
                existing.configValue
            } else {
                val newValue = absentValue.invoke(false)

                this.setSettings(tenantId, key, newValue)

                newValue
            }
        } else {
            val newValue = absentValue.invoke(true)

            this.setSettings(tenantId, key, newValue)

            newValue
        }
    }

    private fun syncToCacheAsync(tenantId: Long) {
        coroutineScope.launch {
            this@TenantSettingsServiceImpl.syncToCache(tenantId)
        }
    }

    private suspend fun syncToCache(tenantId: Long) {
        val settings = getTenantSettings(tenantId)

        redisService.set(
            RedisConstants.getTenantSettingsCacheKey(tenantId),
            settings,
        )

        logger.info("Tenant($tenantId) settings synchronized to cache")
    }

    private fun SystemSettingsItemValueType.matches(raw: String): Boolean = when (this) {
        SystemSettingsItemValueType.STRING -> true
        SystemSettingsItemValueType.NUMBER -> raw.toLongOrNull() != null
        SystemSettingsItemValueType.DECIMAL -> raw.toDoubleOrNull() != null
        SystemSettingsItemValueType.BOOLEAN -> raw.toBooleanStrictOrNull() != null
        SystemSettingsItemValueType.ENUM_SINGLE -> true
        SystemSettingsItemValueType.ENUM_MULTIPLE -> true
    }

    companion object {
        private const val REFRESH_PAYLOAD_DELIMITER = "|"
    }
}
