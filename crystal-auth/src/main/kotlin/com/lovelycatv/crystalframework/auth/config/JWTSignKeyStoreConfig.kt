package com.lovelycatv.crystalframework.auth.config

import com.lovelycatv.crystalframework.auth.stores.JWTSignKeyStore
import com.lovelycatv.crystalframework.shared.service.redis.ReactiveRedisService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

@Configuration
class JWTSignKeyStoreConfig {
    @Bean
    fun jwtSignKeyStore(reactiveRedisService: ReactiveRedisService): JWTSignKeyStore {
        return JWTSignKeyStore(
            store = reactiveRedisService.asReactiveKVStore(),
            keyGenerator = {
                UUID.randomUUID().toString()
            }
        )
    }
}