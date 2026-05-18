package com.lovelycatv.crystalframework.shared.api.system

import com.lovelycatv.crystalframework.shared.constants.RedisConstants
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.types.system.SystemSettings
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class SystemModuleClient(
    private val redisService: RedisService,
    private val reactiveRedisService: ReactiveRedisService
) {
    fun getSystemSettings(throwOnNull: Throwable? = null): SystemSettings? {
        return redisService.get(RedisConstants.SYSTEM_SETTINGS)
    }

    fun getSystemSettingsMono(): Mono<SystemSettings> {
        return reactiveRedisService.get(RedisConstants.SYSTEM_SETTINGS)
    }
}