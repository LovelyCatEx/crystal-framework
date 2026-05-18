package com.lovelycatv.crystalframework.shared.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.RedisSerializer
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import tools.jackson.module.kotlin.kotlinModule

@Configuration
class RedisConfig {

    private fun createJsonRedisSerializer(): GenericJacksonJsonRedisSerializer {
        return GenericJacksonJsonRedisSerializer.builder()
            .enableDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                    .allowIfBaseType(Any::class.java)
                    .build()
            )
            .customize { builder ->
                builder.addModule(kotlinModule())
            }
            .build()
    }

    @Bean
    fun reactiveRedisTemplate(
        reactiveRedisConnectionFactory: ReactiveRedisConnectionFactory,
    ): ReactiveRedisTemplate<String, Any> {
        val stringSerializer = RedisSerializer.string()
        val jsonSerializer = createJsonRedisSerializer()

        val serializationContext = RedisSerializationContext
            .newSerializationContext<String, Any>()
            .key(stringSerializer)
            .value(jsonSerializer)
            .hashKey(stringSerializer)
            .hashValue(jsonSerializer)
            .build()
        return ReactiveRedisTemplate(reactiveRedisConnectionFactory, serializationContext)
    }

    @Bean
    fun reactiveRedisMessageListenerContainer(
        reactiveRedisConnectionFactory: ReactiveRedisConnectionFactory,
    ): ReactiveRedisMessageListenerContainer {
        return ReactiveRedisMessageListenerContainer(reactiveRedisConnectionFactory)
    }

    @Bean
    fun redisTemplate(
        redisConnectionFactory: RedisConnectionFactory,
    ): RedisTemplate<String, Any> {
        val jsonSerializer = createJsonRedisSerializer()

        val template = RedisTemplate<String, Any>()
        template.connectionFactory = redisConnectionFactory
        template.keySerializer = RedisSerializer.string()
        template.valueSerializer = jsonSerializer
        template.hashKeySerializer = RedisSerializer.string()
        template.hashValueSerializer = jsonSerializer
        template.afterPropertiesSet()
        return template
    }
}
