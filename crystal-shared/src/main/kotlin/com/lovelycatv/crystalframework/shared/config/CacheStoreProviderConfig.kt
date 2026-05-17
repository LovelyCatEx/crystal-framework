package com.lovelycatv.crystalframework.shared.config

import com.lovelycatv.crystalframework.shared.service.redis.RedisService
import com.lovelycatv.crystalframework.shared.store.ServiceCacheStore
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CacheStoreProviderConfig {
    @Bean
    @ConditionalOnMissingBean
    fun serviceCacheStore(redisService: RedisService): ServiceCacheStore {
        return ServiceCacheStore(redisService.asKVStore())
    }
}