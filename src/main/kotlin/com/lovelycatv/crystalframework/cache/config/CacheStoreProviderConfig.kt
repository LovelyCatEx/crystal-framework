package com.lovelycatv.crystalframework.cache.config

import com.lovelycatv.crystalframework.cache.store.ServiceCacheStore
import com.lovelycatv.crystalframework.shared.service.redis.RedisService
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