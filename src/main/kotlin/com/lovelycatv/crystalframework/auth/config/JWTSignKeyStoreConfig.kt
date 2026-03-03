package com.lovelycatv.crystalframework.auth.config

import com.lovelycatv.crystalframework.auth.stores.JWTSignKeyStore
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

@Configuration
class JWTSignKeyStoreConfig {
    @Bean
    fun jwtSignKeyStore(redisService: RedisService): JWTSignKeyStore {
        return JWTSignKeyStore(
            store = redisService.asKVStore(),
            keyGenerator = {
                UUID.randomUUID().toString()
            }
        )
    }
}