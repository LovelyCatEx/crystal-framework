package com.lovelycatv.crystalframework.shared.api.system

import com.lovelycatv.crystalframework.shared.constants.RedisConstants
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.types.system.SystemSettings
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class SystemModuleClient(
    private val reactiveRedisService: ReactiveRedisService
) {
    /**
     * Synchronous accessor for non-suspend boundaries (Spring SPIs, AOP aspects). Bridges the
     * reactive read with [runBlocking] as mandated for those call sites.
     */
    fun getSystemSettings(throwOnNull: Throwable? = null): SystemSettings? {
        val settings = runBlocking {
            reactiveRedisService.get<SystemSettings>(RedisConstants.SYSTEM_SETTINGS).awaitFirstOrNull()
        }
        if (settings == null && throwOnNull != null) {
            throw throwOnNull
        }
        return settings
    }

    fun getSystemSettingsMono(): Mono<SystemSettings> {
        return reactiveRedisService.get(RedisConstants.SYSTEM_SETTINGS)
    }
}
