package com.lovelycatv.template.springboot.shared.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.RedisSerializer

@Configuration
class RedisConfig {
    @Bean
    fun reactiveRedisTemplate(
        reactiveRedisConnectionFactory: ReactiveRedisConnectionFactory,
        // objectMapper: tools.jackson.databind.ObjectMapper,
    ): ReactiveRedisTemplate<String, Any> {
        val stringSerializer = RedisSerializer.string()

        val jsonSerializer = JacksonJsonRedisSerializer(Any::class.java)
        // val jsonSerializer = GenericJacksonJsonRedisSerializer(objectMapper)

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