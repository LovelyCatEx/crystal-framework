package com.lovelycatv.crystalframework.shared.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.RedisSerializer

@Configuration
class RedisConfig {
    @Bean
    fun reactiveRedisTemplate(
        reactiveRedisConnectionFactory: ReactiveRedisConnectionFactory,
    ): ReactiveRedisTemplate<String, Any> {
        val stringSerializer = RedisSerializer.string()

        // 使用默认的 GenericJackson2JsonRedisSerializer，它会自动配置 @class 类型信息
        val jsonSerializer = GenericJackson2JsonRedisSerializer()

        val serializationContext = RedisSerializationContext
            .newSerializationContext<String, Any>()
            .key(stringSerializer)
            .value(jsonSerializer)
            .hashKey(stringSerializer)
            .hashValue(jsonSerializer)
            .build()
        return ReactiveRedisTemplate(reactiveRedisConnectionFactory, serializationContext)
    }
}
