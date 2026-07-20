package com.lovelycatv.crystalframework.tenant.settings.service.impl

import com.lovelycatv.crystalframework.sdk.common.settings.matches
import com.lovelycatv.crystalframework.sdk.tenant.settings.TenantSettingsRegistry
import com.lovelycatv.crystalframework.shared.constants.RedisConstants
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.tenant.settings.entity.TenantSettingsEntity
import com.lovelycatv.crystalframework.tenant.settings.repository.TenantSettingsRepository
import com.lovelycatv.crystalframework.tenant.settings.service.TenantSettingsService
import com.lovelycatv.crystalframework.tenant.settings.constants.TenantSettingsConstants
import com.lovelycatv.crystalframework.tenant.settings.types.TenantSettingsView
import com.lovelycatv.crystalframework.shared.store.ReactiveExpiringKVStore
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

private const val SECRET_MASK = "***"

@Service
class TenantSettingsServiceImpl(
    private val tenantSettingsRepository: TenantSettingsRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val reactiveRedisService: ReactiveRedisService,
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

    override val cacheStore: ReactiveExpiringKVStore<String, TenantSettingsEntity>
        get() = reactiveRedisService.asReactiveKVStore()
    override val listCacheStore: ReactiveExpiringKVStore<String, List<TenantSettingsEntity>>
        get() = reactiveRedisService.asReactiveKVStore()
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
                memberJoin = TenantSettingsView.Notification.MemberJoin(
                    email = getSettings(tenantId, TenantSettingsConstants.Notification.MemberJoin.EMAIL)!!,
                    channels = getSettings(tenantId, TenantSettingsConstants.Notification.MemberJoin.CHANNELS)!!,
                    content = getSettings(tenantId, TenantSettingsConstants.Notification.MemberJoin.CONTENT)!!,
                ),
                memberJoinReview = TenantSettingsView.Notification.MemberJoinReview(
                    email = getSettings(tenantId, TenantSettingsConstants.Notification.MemberJoinReview.EMAIL)!!,
                    channels = getSettings(tenantId, TenantSettingsConstants.Notification.MemberJoinReview.CHANNELS)!!,
                    content = getSettings(tenantId, TenantSettingsConstants.Notification.MemberJoinReview.CONTENT)!!,
                ),
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

            logger.info("Tenant($tenantId) settings $key updated to ${displayValue(key, value)}")
        } else {
            this.getRepository().save(
                TenantSettingsEntity(
                    id = snowIdGenerator.nextId(),
                    tenantId = tenantId,
                    configKey = key,
                    configValue = value,
                ) newEntity true
            ).awaitFirstOrNull()

            logger.info("Tenant($tenantId) settings $key saved, value: ${displayValue(key, value)}")
        }
    }

    private fun displayValue(key: String, value: String?): String? {
        if (value.isNullOrBlank()) return value
        val declaration = tenantSettingsRegistry.settingDeclarations().firstOrNull { it.key == key }
        return if (declaration?.isSecret == true) SECRET_MASK else value
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

        reactiveRedisService.set(
            RedisConstants.getTenantSettingsCacheKey(tenantId),
            settings,
        ).awaitFirstOrNull()

        logger.info("Tenant($tenantId) settings synchronized to cache")
    }

    companion object {
        private const val REFRESH_PAYLOAD_DELIMITER = "|"
    }
}
