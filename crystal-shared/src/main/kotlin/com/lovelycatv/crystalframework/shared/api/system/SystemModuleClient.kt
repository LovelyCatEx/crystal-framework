package com.lovelycatv.crystalframework.shared.api.system

import com.lovelycatv.crystalframework.shared.constants.RedisConstants
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.types.system.SystemSettings
import com.lovelycatv.vertex.log.logger
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Read-only accessor to the shared system settings cache for modules that cannot depend on the
 * system module directly.
 *
 * The settings are held in memory so synchronous, non-suspend boundaries (Spring SPIs, AOP
 * aspects, the auth filter chain) never block a reactor event-loop thread on Redis. The cache is
 * warmed at startup on the boot thread and invalidated via the system-settings refresh topic, so
 * the hot path is a pure in-memory read.
 */
@Component
class SystemModuleClient(
    private val reactiveRedisService: ReactiveRedisService,
    private val reactiveRedisMessageListenerContainer: ReactiveRedisMessageListenerContainer,
) {
    private val logger = logger()

    private val refreshTopic = ChannelTopic(RedisConstants.SYSTEM_SETTINGS_REFRESH_TOPIC)

    @Volatile
    private var cachedSystemSettings: SystemSettings? = null

    @PostConstruct
    fun init() {
        runBlocking {
            try {
                reload()
            } catch (e: Exception) {
                logger.warn("Failed to deserialize cached system settings, invalidating stale cache", e)
                reactiveRedisService.removeKey(RedisConstants.SYSTEM_SETTINGS).awaitFirstOrNull()
                cachedSystemSettings = null
            }
        }

        reactiveRedisMessageListenerContainer
            .receive(refreshTopic)
            .subscribe {
                logger.info("Received system settings refresh signal, invalidating SystemModuleClient cache")
                cachedSystemSettings = null
            }
    }

    /**
     * In-memory read for non-suspend boundaries. Lazily reloads from Redis only after a refresh
     * signal invalidated the cache — never once per request under steady state.
     */
    fun getSystemSettings(throwOnNull: Throwable? = null): SystemSettings? {
        val settings = cachedSystemSettings ?: runBlocking { reload() }
        if (settings == null && throwOnNull != null) {
            throw throwOnNull
        }
        return settings
    }

    fun getSystemSettingsMono(): Mono<SystemSettings> {
        return reactiveRedisService.get(RedisConstants.SYSTEM_SETTINGS)
    }

    private suspend fun reload(): SystemSettings? {
        return try {
            reactiveRedisService
                .get<SystemSettings>(RedisConstants.SYSTEM_SETTINGS)
                .awaitFirstOrNull()
                .also { cachedSystemSettings = it }
        } catch (e: Exception) {
            logger.warn("Failed to deserialize system settings from cache, treating as cache miss", e)
            reactiveRedisService.removeKey(RedisConstants.SYSTEM_SETTINGS).awaitFirstOrNull()
            cachedSystemSettings = null
            null
        }
    }
}
